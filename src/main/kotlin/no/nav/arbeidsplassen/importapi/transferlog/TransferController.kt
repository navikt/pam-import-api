package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.AdStatus
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.feltFraPathReference
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.toMD5Hex
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/transfers")
@SecurityRequirement(name = "bearer-auth")
class TransferController(
    private val transferLogService: TransferLogService,
    private val providerService: ProviderService,
    private val adStateService: AdStateService,
    private val objectMapper: ObjectMapper,
    @Value("\${transferlog.batch-size:100}") val adsSize: Int
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferController::class.java)
    }

    @Post("/batch/{providerId}")
    fun postTransfer(@PathVariable providerId: Long, @Body ads: List<AdDTO>): HttpResponse<TransferLogDTO> {
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
            return HttpResponse.ok(
                TransferLogDTO(
                    message = "Content already exist, skipping",
                    status = TransferLogStatus.SKIPPED,
                    items = 1,
                    md5 = md5,
                    providerId = provider.id!!
                )
            )
        }

        updatedAds.stream().forEach {
            LOG.info("Got ad ${it.reference} for $providerId")
            transferLogService.validate(it)
        }

        val transferLogDTO = TransferLogDTO(payload = content, md5 = md5, items = ads.size, providerId = provider.id!!)
        return HttpResponse.created(transferLogService.save(transferLogDTO).apply {
            payload = null
        })
    }

    @Post(value = "/{providerId}", processes = [MediaType.APPLICATION_JSON_STREAM])
    fun postStream(@PathVariable providerId: Long, @Body json: Flowable<JsonNode>): Flowable<TransferLogDTO> {
        val provider = providerService.findById(providerId)
        LOG.info("Streaming for provider $providerId")
        return json.subscribeOn(Schedulers.io()).map {
            runCatching {
                var ad = objectMapper.treeToValue(it, AdDTO::class.java)
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
                        providerId = provider.id!!
                    )
                } else {
                    transferLogService.validate(ad)
                    transferLogService.save(
                        TransferLogDTO(
                            payload = content,
                            md5 = md5,
                            items = 1,
                            providerId = provider.id!!
                        )
                    ).apply {
                        payload = null
                    }
                }
            }.getOrElse { handleError(it, provider) }
        }
    }

    private fun handleError(error: Throwable, provider: ProviderDTO): TransferLogDTO {
        val transferLogDTO = when (error) {
            is JsonParseException -> TransferLogDTO(
                message = "Parse error: at ${error.location}",
                status = TransferLogStatus.ERROR,
                providerId = provider.id!!
            )

            is InvalidFormatException -> TransferLogDTO(
                message = "Invalid value: ${error.value} at ${feltFraPathReference(error.pathReference)}",
                status = TransferLogStatus.ERROR,
                providerId = provider.id!!
            )

            is InvalidNullException -> TransferLogDTO(
                    message = "Missing parameter: ${error.propertyName.simpleName}",
                    status = TransferLogStatus.ERROR,
                    providerId = provider.id!!
            )

            is MismatchedInputException -> TransferLogDTO(
                    message = "Missing parameter: ${feltFraPathReference(error.pathReference)}",
                    status = TransferLogStatus.ERROR,
                    providerId = provider.id!!
            )

            else -> TransferLogDTO(
                message = "Error: ${error.localizedMessage}",
                status = TransferLogStatus.ERROR,
                providerId = provider.id!!
            )
        }
        LOG.error("Exception {} providerId: {}", transferLogDTO.message, provider.id)
        return transferLogDTO
    }

    @Get("/{providerId}/versions/{versionId}")
    fun getTransfer(@PathVariable providerId: Long, @PathVariable versionId: Long): TransferLogDTO {
        return transferLogService.findByVersionIdAndProviderId(versionId, providerId)
    }

    @Get("/{providerId}/versions/{versionId}/payload")
    fun getTransferPayload(@PathVariable providerId: Long, @PathVariable versionId: Long): List<AdDTO> {
        return objectMapper.readValue(
            transferLogService.findByVersionIdAndProviderId(versionId, providerId).payload,
            object : TypeReference<List<AdDTO>>() {})
    }

    @Delete("/{providerId}/{reference}")
    fun stopAdByProviderReference(
        @PathVariable providerId: Long, @PathVariable reference: String,
        @QueryValue(defaultValue = "false") delete: Boolean
    ): TransferLogDTO {
        LOG.info("stopAdByProviderReference providerId: {} reference: {}, delete: {}", providerId, reference, delete)
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        val adStatus = if (delete) AdStatus.DELETED else AdStatus.STOPPED
        val ad = adState.ad.copy(expires = LocalDateTime.now().minusMinutes(1), status = adStatus)
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val provider = providerService.findById(providerId)
        return transferLogService.save(
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
    }

}
