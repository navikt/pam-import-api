package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.ImportApiError
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.AdStatus
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.toMD5Hex
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/transfers")
class TransferController(private val transferLogService: TransferLogService,
                         private val providerService: ProviderService,
                         private val adStateService: AdStateService,
                         private val objectMapper: ObjectMapper,
                         private val styrkCodeConverter: StyrkCodeConverter,
                         @Value("\${transferlog.batch-size:500}") val adsSize: Int) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferController::class.java)
    }

    @Post("/{providerId}")
    fun postTransfer(@PathVariable providerId: Long, @Body ads: List<AdDTO>): HttpResponse<TransferLogDTO> {
        // TODO authorized provider access here
        if (ads.size > adsSize || ads.size < 1) {
            throw ImportApiError("ads should be between 1 to max $adsSize", ErrorType.INVALID_VALUE)
        }
        val content = objectMapper.writeValueAsString(ads)
        val md5 = content.toMD5Hex()
        val provider = providerService.findById(providerId)
        if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
             return HttpResponse.ok(TransferLogDTO(message = "Content already exist, skipping", status = TransferLogStatus.SKIPPED, items = 1, provider = provider, md5 = md5))
        }
        ads.stream().forEach { validate(it) }

        val transferLogDTO = TransferLogDTO(provider = provider, payload = content, md5 = md5, items = ads.size)
        return HttpResponse.created(transferLogService.save(transferLogDTO).apply {
            payload = null
        })

    }

    @Post(value = "/{providerId}", processes = [MediaType.APPLICATION_JSON_STREAM])
    fun postStream(@PathVariable providerId: Long, @Body ad: Flowable<AdDTO>): Flowable<TransferLogDTO> {
        val provider = providerService.findById(providerId)
        LOG.debug("Streaming for provider $providerId")
        return ad.subscribeOn(Schedulers.io()).map {
            LOG.info("Got ad ${it.reference} for $providerId")
            val content = objectMapper.writeValueAsString(it)
            val md5 = content.toMD5Hex()
            if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
                TransferLogDTO(message = "Content already exist, skipping", status = TransferLogStatus.SKIPPED, items = 1, provider = provider, md5 = md5)
            }
            else {
                validate(it)
                transferLogService.save(TransferLogDTO(provider = provider, payload = content, md5 = md5, items = 1)).apply {
                    payload = null
                }
            }
        }
    }

    @Get("/{providerId}/versions/{versionId}")
    fun getTransfer(@PathVariable providerId: Long, @PathVariable versionId: Long): HttpResponse<TransferLogDTO> {
        return HttpResponse.ok(transferLogService.findByVersionIdAndProviderId(versionId, providerId))
    }

    @Delete("/{providerId}/{reference}")
    fun stopAdByProviderReference(@PathVariable providerId: Long, @PathVariable reference: String,
                                  @QueryValue(defaultValue = "false") delete: Boolean): HttpResponse<TransferLogDTO> {
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        val adStatus = if (delete) AdStatus.DELETED else AdStatus.STOPPED
        val ad = adState.ad.copy(expires = LocalDateTime.now().minusMinutes(1), status = adStatus)
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val provider = providerService.findById(providerId)
        val transferLogDTO = TransferLogDTO(provider = provider, payload = jsonPayload, md5 = md5, items = 1)
        return HttpResponse.ok(transferLogService.save(transferLogDTO))
    }

    private fun validate(ad: AdDTO) {
        // validate category
        ad.categoryList.stream().forEach { cat ->
            styrkCodeConverter.lookup(cat.code)
                    .orElseThrow { ImportApiError("Category code ${cat.code} is not found", ErrorType.INVALID_VALUE) }
        }
    }

}