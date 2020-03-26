package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.ImportApiError
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.toMD5Hex
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import java.time.LocalDateTime
import javax.annotation.security.RolesAllowed

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/transfers")
class TransferController(private val transferLogService: TransferLogService,
                         private val providerService: ProviderService,
                         private val adStateService: AdStateService,
                         private val objectMapper: ObjectMapper,
                         private val styrkCodeConverter: StyrkCodeConverter,
                         @Value("\${adsSize:100}") val adsSize: Int = 100) {

    @Post("/{providerId}")
    fun postTransfer(@PathVariable providerId: Long, @Body ads: List<AdDTO>): HttpResponse<TransferLogDTO> {
        // TODO authorized provider access here
        if (ads.size > adsSize || ads.size < 1) {
            throw ImportApiError("ads should be between 1 to max $adsSize", ErrorType.INVALID_VALUE)
        }
        val content = objectMapper.writeValueAsString(ads)
        val md5 = content.toMD5Hex()
        val provider = providerService.findById(providerId)
        val transferLogDTO = TransferLogDTO(provider = provider, payload = content, md5 = md5, items = ads.size)
        validateContent(providerId, md5, ads)
        return HttpResponse.created(transferLogService.saveTransfer(transferLogDTO).apply {
            payload = null
        })

    }

    @Get("/{providerId}/versions/{versionId}")
    fun getTransfer(@PathVariable providerId: Long, @PathVariable versionId: Long): Single<HttpResponse<TransferLogDTO>> {
        return Single.just(HttpResponse.ok(transferLogService.findByVersionIdAndProviderId(versionId, providerId)))
    }

    @Delete("/{providerId}/{reference}")
    fun stopAdByProviderReference(@PathVariable providerId: Long, @PathVariable reference: String): Single<HttpResponse<TransferLogDTO>> {
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        // set expire to now-5min, so that this ad will be "deleted"
        val ad = adState.ad.copy(expires = LocalDateTime.now().minusMinutes(5))
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val provider = providerService.findById(providerId)
        val transferLogDTO = TransferLogDTO(provider = provider, payload = jsonPayload, md5 = md5, items = 1)
        return Single.just(HttpResponse.ok(transferLogService.saveTransfer(transferLogDTO)))
    }

    private fun validateContent(providerId: Long, md5: String, it: List<AdDTO>) {
        if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
            throw ImportApiError("Content already exists", ErrorType.CONFLICT)
        }
        it.stream().forEach {
            it.categoryList.stream().forEach { cat ->
                styrkCodeConverter.lookup(cat.code)
                        .orElseThrow { ImportApiError("Category code ${cat.code} is not found", ErrorType.INVALID_VALUE) }
            }
        }
    }
}