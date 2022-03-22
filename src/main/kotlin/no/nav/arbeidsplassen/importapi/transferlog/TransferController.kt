package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.toMD5Hex
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/transfers")
@SecurityRequirement(name = "bearer-auth")
class TransferController(private val transferLogService: TransferLogService,
                         private val providerService: ProviderService,
                         private val adStateService: AdStateService,
                         private val objectMapper: ObjectMapper,
                         private val styrkCodeConverter: StyrkCodeConverter,
                         private val propertyNameValueValidation: PropertyNameValueValidation,
                         @Value("\${transferlog.batch-size:100}") val adsSize: Int) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferController::class.java)
    }

    @Post("/batch/{providerId}")
    fun postTransfer(@PathVariable providerId: Long, @Body ads: List<AdDTO>): HttpResponse<TransferLogDTO> {
        if (ads.size > adsSize || ads.isEmpty()) {
            throw ImportApiError("ads should be between 1 to max $adsSize", ErrorType.INVALID_VALUE)
        }
        val content = objectMapper.writeValueAsString(ads)
        val md5 = content.toMD5Hex()
        val provider = providerService.findById(providerId)
        if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
             return HttpResponse.ok(TransferLogDTO(message = "Content already exist, skipping", status = TransferLogStatus.SKIPPED, items = 1, md5 = md5, providerId = provider.id!!))
        }
        ads.stream().forEach { validate(it) }

        val transferLogDTO = TransferLogDTO(payload = content, md5 = md5, items = ads.size, providerId = provider.id!!)
        return HttpResponse.created(transferLogService.save(transferLogDTO).apply {
            payload = null
        })

    }

    @Post(value = "/{providerId}", processes = [MediaType.APPLICATION_JSON_STREAM])
    fun postStream(@PathVariable providerId: Long, @Body json: Flowable<JsonNode>): Flowable<TransferLogDTO> {
        val provider = providerService.findById(providerId)
        LOG.debug("Streaming for provider $providerId")
        return json.subscribeOn(Schedulers.io()).map {
            runCatching {
                val ad = objectMapper.treeToValue(it, AdDTO::class.java)
                LOG.info("Got ad ${ad.reference} for $providerId")
                val content = objectMapper.writeValueAsString(ad)
                val md5 = content.toMD5Hex()
                if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
                    TransferLogDTO(message = "Content already exist, skipping", status = TransferLogStatus.SKIPPED, items = 1, md5 = md5, providerId = provider.id!!)
                }
                else {
                    validate(ad)
                    transferLogService.save(TransferLogDTO(payload = content, md5 = md5, items = 1, providerId = provider.id!!)).apply {
                        payload = null
                    }
                }
            }.getOrElse { handleError(it, provider) }
        }
    }

    private fun locationMustHavePostalCodeOrCountyMunicipal(ad:AdDTO) = (ad.locationList.isNotEmpty()
            && (!ad.locationList[0].postalCode.isNullOrEmpty() ||
            (!ad.locationList[0].county.isNullOrEmpty() && !ad.locationList[0].municipal.isNullOrEmpty())))

    private fun handleError(error: Throwable, provider: ProviderDTO): TransferLogDTO {
       return when (error) {
            is JsonParseException -> TransferLogDTO(message="Parse error: at ${error.location}", status = TransferLogStatus.ERROR, providerId = provider.id!!)
            is MissingKotlinParameterException -> TransferLogDTO(message="Missing parameter: ${error.parameter.name}", status = TransferLogStatus.ERROR, providerId = provider.id!!)
            is InvalidFormatException ->TransferLogDTO(message="Invalid value: ${error.value} at ${error.pathReference}", status = TransferLogStatus.ERROR, providerId = provider.id!!)
            else -> TransferLogDTO(message="Error: ${error.localizedMessage}", status = TransferLogStatus.ERROR,providerId = provider.id!!)
        }
    }

    @Get("/{providerId}/versions/{versionId}")
    fun getTransfer(@PathVariable providerId: Long, @PathVariable versionId: Long): TransferLogDTO {
        return transferLogService.findByVersionIdAndProviderId(versionId, providerId)
    }

    @Get("/{providerId}/versions/{versionId}/payload")
    fun getTransferPayload(@PathVariable providerId: Long, @PathVariable versionId: Long): List<AdDTO> {
        return objectMapper.readValue(transferLogService.findByVersionIdAndProviderId(versionId, providerId).payload, object: TypeReference<List<AdDTO>>(){})
    }

    @Delete("/{providerId}/{reference}")
    fun stopAdByProviderReference(@PathVariable providerId: Long, @PathVariable reference: String,
                                  @QueryValue(defaultValue = "false") delete: Boolean): TransferLogDTO {
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        val adStatus = if (delete) AdStatus.DELETED else AdStatus.STOPPED
        val ad = adState.ad.copy(expires = LocalDateTime.now().minusMinutes(1), status = adStatus)
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val provider = providerService.findById(providerId)
        return transferLogService.save(TransferLogDTO(message = adStatus.name , payload = jsonPayload, md5 = md5, items = 1,providerId = provider.id!!)).apply {
            payload = null
        }
    }

    private fun validate(ad: AdDTO) {
        if (ad.categoryList.count()>3) {
            throw ImportApiError("category list is over 3, we only allow max 3 categories per ad", ErrorType.INVALID_VALUE)
        }
        if (!locationMustHavePostalCodeOrCountyMunicipal(ad)) {
            throw ImportApiError("Location does not have postal code, or does not have county/municipality", ErrorType.INVALID_VALUE)
        }
        ad.categoryList.stream().forEach { cat ->
            val optCat = styrkCodeConverter.lookup(cat.code)
            if (optCat.isEmpty) throw ImportApiError("category ${cat.code} does not exist", ErrorType.INVALID_VALUE)
        }
        propertyNameValueValidation.checkOnlyValidValues(ad.properties)
    }

}
