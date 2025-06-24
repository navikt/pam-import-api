package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiRequestBody
import io.javalin.openapi.OpenApiResponse
import io.javalin.openapi.OpenApiSecurity
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.common.toMD5Hex
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.AdStatus
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType
import no.nav.arbeidsplassen.importapi.exception.feltFraPathReference
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class TransferController(
    private val transferLogService: TransferLogService,
    private val providerService: ProviderService,
    private val adStateService: AdStateService,
    private val objectMapper: ObjectMapper,
    val adsSize: Int // @Value("\${transferlog.batch-size:100}")
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferController::class.java)

        private fun Context.providerIdParam(): Long = pathParam("providerId").toLong()
        private fun Context.referenceParam(): String = pathParam("reference")
        private fun Context.deleteParam(): Boolean = queryParam("delete")?.toBoolean() ?: false
        private fun Context.versionIdParam(): Long = pathParam("versionId").toLong()
        private fun Context.adListBody(): List<AdDTO> {
            val typeReference = object : TypeReference<List<AdDTO>>() {}
            val ads: List<AdDTO> = this.bodyAsClass(typeReference.type)
            return ads
        }
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.post(
            "/api/v1/transfers/batch/{providerId}",
            { postTransfer(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
        javalin.post(
            "/api/v1/transfers/{providerId}",
            { streamTransfer(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
        javalin.get(
            "/api/v1/transfers/{providerId}/versions/{versionId}",
            { getTransfer(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
        javalin.get(
            "/api/v1/transfers/{providerId}/versions/{versionId}/payload",
            { getTransferPayload(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
        javalin.delete(
            "/api/v1/transfers/{providerId}/{reference}",
            { stopAdByProviderReference(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/transfers/batch/{providerId}",
        methods = [HttpMethod.POST],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId")
        ],
        requestBody = OpenApiRequestBody(
            required = true,
            content = [OpenApiContent(from = Array<AdDTO>::class)]
        ),
        responses = [
            OpenApiResponse(
                status = "200",
                description = "postTransfer 200 response",
                content = [OpenApiContent(from = TransferLogDTO::class)]
            ),
        ]
    )
    private fun postTransfer(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val ads = ctx.adListBody()
        LOG.info("Streaming ${ads.size} for provider $providerId")

        if (ads.size > adsSize || ads.isEmpty()) {
            throw ImportApiError("ads should be between 1 to max $adsSize", ErrorType.INVALID_VALUE)
        }
        val updatedAds = ads.map {
            transferLogService.handleExpiryAndStarttimeCombinations(it)
        }.map {
            transferLogService.handleInvalidCategories(it, providerId, it.reference)
        }

        val content = objectMapper.writeValueAsString(updatedAds)
        val md5 = content.toMD5Hex()
        val provider = providerService.findById(providerId)
        if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
            val transferLog = TransferLogDTO(
                message = "Content already exist, skipping",
                status = TransferLogStatus.SKIPPED,
                items = 1,
                md5 = md5,
                providerId = provider.id!!
            )
            ctx.status(HttpStatus.OK).json(transferLog)
            return
        }

        updatedAds.stream().forEach {
            LOG.info("Got ad ${it.reference} for $providerId")
            transferLogService.validate(it)
        }

        val transferLogDTO = TransferLogDTO(payload = content, md5 = md5, items = ads.size, providerId = provider.id!!)
        ctx.status(HttpStatus.CREATED).json(transferLogService.save(transferLogDTO).apply { payload = null })
    }

    // @Post(value = "/{providerId}", processes = [MediaType.APPLICATION_JSON_STREAM])
    @OpenApi(
        path = "/stillingsimport/api/v1/transfers/{providerId}",
        methods = [HttpMethod.POST],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId")
        ],
        requestBody = OpenApiRequestBody(
            required = true,
            // TODO Micronaut gir her ut ingenting, som jo åpenbart er feil. Dette er heller ikke riktig, men bedre..
            content = [OpenApiContent(mimeType = "application/x-json-stream", from = AdDTO::class)]
        ),
        responses = [
            OpenApiResponse(
                status = "200",
                description = "postStream 200 response",
                // TODO: Dette er det Micronaut gir ut, men det er vel strengt tatt ikke helt korrekt
                content = [OpenApiContent(from = TransferLogDTO::class)]
            ),
        ]
    )
    private fun streamTransfer(ctx: Context) {
        // Når man leser denne metoden så kan man kanskje reagere på at feilsituasjoner håndteres litt ulikt
        // Dette er et valg tatt for å emulere hvordan det var i Micronaut.
        val providerId = ctx.providerIdParam()
        LOG.info("Streaming for provider $providerId")

        ctx.bodyInputStream().use { inputStream ->
            val parser: JsonParser = JsonFactory().createParser(inputStream)
            var token: JsonToken? = try {
                parser.nextToken()
            } catch (ex: JsonParseException) {
                // Kommer hvis inputstreamen inneholder gibberish
                ctx
                    .status(200)
                    .outputStream().use { outputStream ->
                        objectMapper.writeValue(outputStream, handleError(ex, providerId))
                    }
                return
            }

            if (token == null) {
                // Kommer hvis inputstreamen er tom
                ctx.status(400)
                return
            }

            ctx
                .status(200)
                .outputStream().use { outputStream ->
                    // Den følgende koden har tatt inspirasjon herfra, men er så endret en del:
                    // https://sohlich.github.io/post/jackson/
                    while (token != null) {
                        try {
                            val node: JsonNode = objectMapper.readValue(parser, JsonNode::class.java)
                            val response = receiveAd(node, providerId)
                            LOG.info("Skriver response til outputstream")
                            objectMapper.writeValue(outputStream, response)

                            LOG.info("Leser neste token")
                            token = parser.nextToken()
                        } catch (e: JsonParseException) {
                            LOG.error("JsonParseException", e)
                            objectMapper.writeValue(outputStream, handleError(e, providerId))
                            break
                        }
                    }
                }
        }
    }

    private fun receiveAd(jsonNode: JsonNode, providerId: Long): TransferLogDTO {
        return runCatching {
            LOG.info("Received ad for provider $providerId")
            var ad = objectMapper.treeToValue(jsonNode, AdDTO::class.java)
            LOG.info("Got ad ${ad.reference} for $providerId")
            ad = transferLogService.handleExpiryAndStarttimeCombinations(ad)
            ad = transferLogService.handleInvalidCategories(ad, providerId, ad.reference)
            val content = objectMapper.writeValueAsString(ad)
            val md5 = content.toMD5Hex()
            if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
                TransferLogDTO(
                    message = "Content already exist, skipping",
                    status = TransferLogStatus.SKIPPED,
                    items = 1,
                    md5 = md5,
                    providerId = providerId
                )
            } else {
                transferLogService.validate(ad)
                transferLogService.save(
                    TransferLogDTO(
                        payload = content,
                        md5 = md5,
                        items = 1,
                        providerId = providerId
                    )
                ).apply {
                    payload = null
                }
            }
        }.getOrElse { handleError(it, providerId) }
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/transfers/{providerId}/versions/{versionId}",
        methods = [HttpMethod.GET],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "versionId", type = Long::class, required = true, description = "versionId")
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getTransfer 200 response",
                content = [OpenApiContent(from = TransferLogDTO::class)]
            ),
        ]
    )
    private fun getTransfer(ctx: Context) {
        val providerId: Long = ctx.providerIdParam()
        val versionId: Long = ctx.versionIdParam()
        ctx.status(HttpStatus.OK).json(transferLogService.findByVersionIdAndProviderId(versionId, providerId))
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/transfers/{providerId}/versions/{versionId}/payload",
        methods = [HttpMethod.GET],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "versionId", type = Long::class, required = true, description = "versionId")
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getTransferPayload 200 response",
                content = [OpenApiContent(from = Array<AdDTO>::class)]
            ),
        ]
    )
    private fun getTransferPayload(ctx: Context) {
        val providerId: Long = ctx.providerIdParam()
        val versionId: Long = ctx.versionIdParam()
        val payload = transferLogService.findByVersionIdAndProviderId(versionId, providerId).payload
        val adList: List<AdDTO> =
            objectMapper.readValue(payload, object : TypeReference<List<AdDTO>>() {}) ?: emptyList()
        ctx.status(HttpStatus.OK).json(adList)
    }


    @OpenApi(
        path = "/stillingsimport/api/v1/transfers/{providerId}/{reference}",
        methods = [HttpMethod.DELETE],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "reference", type = String::class, required = true, description = "reference")
        ],
        queryParams = [
            OpenApiParam(
                name = "delete",
                type = Boolean::class,
                required = false,
                description = "Default value : false"
            ),
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "stopAdByProviderReference 200 response. TransferLogDTO.payload is set to null",
                content = [OpenApiContent(from = TransferLogDTO::class)]
            ),
        ]
    )
    
    private fun stopAdByProviderReference(ctx: Context) {
        val providerId: Long = ctx.providerIdParam()
        val reference: String = ctx.referenceParam()
        val delete: Boolean = ctx.deleteParam()

        LOG.info("stopAdByProviderReference providerId: {} reference: {}, delete: {}", providerId, reference, delete)
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        val adStatus = if (delete) AdStatus.DELETED else AdStatus.STOPPED
        val adWithoutInvalidCategories =
            transferLogService.handleInvalidCategories(ad = adState.ad, providerId = providerId, reference = reference)
        val ad = adWithoutInvalidCategories.copy(expires = LocalDateTime.now().minusMinutes(1), status = adStatus)
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val provider = providerService.findById(providerId)
        val transferLog = transferLogService.save(
            TransferLogDTO(
                message = adStatus.name,
                payload = jsonPayload,
                md5 = md5,
                items = 1,
                providerId = provider.id!!
            )
        ).apply {
            payload = null
        }
        ctx.status(HttpStatus.OK).json(transferLog)
    }

    private fun handleError(error: Throwable, providerId: Long): TransferLogDTO {
        val transferLogDTO = when (error) {

            is JsonParseException -> TransferLogDTO(
                message = "JSON Parse error: at ${error.location}",
                status = TransferLogStatus.ERROR,
                providerId = providerId
            )

            is InvalidFormatException -> TransferLogDTO(
                message = "Invalid value: ${error.value} at ${feltFraPathReference(error.pathReference)}",
                status = TransferLogStatus.ERROR,
                providerId = providerId
            )

            is InvalidNullException -> TransferLogDTO(
                message = "Missing parameter: ${error.propertyName.simpleName}",
                status = TransferLogStatus.ERROR,
                providerId = providerId
            )

            is MismatchedInputException -> TransferLogDTO(
                message = "Missing parameter: ${feltFraPathReference(error.pathReference)}",
                status = TransferLogStatus.ERROR,
                providerId = providerId
            )

            else -> TransferLogDTO(
                message = "Error: ${error.localizedMessage}",
                status = TransferLogStatus.ERROR,
                providerId = providerId
            )
        }
        LOG.warn("Exception {} providerId: {}", transferLogDTO.message, providerId)
        return transferLogDTO
    }
}
