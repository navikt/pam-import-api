package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.ImportApiError
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.toMD5Hex
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import java.time.LocalDateTime


@Controller("/api/v1/transfers")
class TransferController(private val transferLogService: TransferLogService,
                         private val providerService: ProviderService,
                         private val adStateService: AdStateService,
                         private val objectMapper: ObjectMapper,
                         @Value("\${adsSize:100}") val adsSize: Int = 100) {

    @Post("/{providerId}")
    fun postTransfer(@PathVariable providerId: Long, @Body upload: Single<List<AdDTO>>): Single<HttpResponse<TransferLogDTO>> {
        return upload.map {
            // TODO authorized provider access here
            if (it.size>adsSize || it.size<1) {
                throw ImportApiError("ads should be between 1 to max $adsSize", ErrorType.INVALID_VALUE)
            }
            val content = objectMapper.writeValueAsString(it)
            val md5 = content.toMD5Hex()
            val transferLogDTO = TransferLogDTO(providerId=providerId, payload = content, md5 = md5)
            if (transferLogService.existsByProviderIdAndMd5(providerId, md5)) {
                throw ImportApiError("Content already exists", ErrorType.CONFLICT)
            }
            HttpResponse.created(transferLogService.saveTransfer(transferLogDTO).apply { this.payload = null })
        }
    }

    @Get("/{versionId}")
    fun getTransfer(@PathVariable versionId: Long): Single<HttpResponse<TransferLogDTO>> {
        return Single.just(HttpResponse.ok(transferLogService.findByVersionId(versionId)))
    }

    @Delete("/{providerId}/{reference}")
    fun stopAdByProviderReference(@PathVariable providerId: Long, @PathVariable reference: String): Single<HttpResponse<TransferLogDTO>> {
        val adState = adStateService.getAdStatesByProviderReference(providerId, reference)
        // set expire to now, so that this ad will be "deleted"
        val ad = adState.ad.copy(expires = LocalDateTime.now().minusSeconds(1))
        val jsonPayload = objectMapper.writeValueAsString(ad)
        val md5 = jsonPayload.toMD5Hex()
        val transferLogDTO = TransferLogDTO(providerId = providerId, payload = jsonPayload, md5 = md5)
        return Single.just(HttpResponse.ok(transferLogService.saveTransfer(transferLogDTO)))
    }

}