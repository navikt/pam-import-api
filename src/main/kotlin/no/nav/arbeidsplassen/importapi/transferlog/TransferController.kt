package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.ApiError
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.md5Hex
import no.nav.arbeidsplassen.importapi.provider.ProviderService


@Controller("/api/v1/transfers")
class TransferController(private val dtoValidation: DTOValidation,
                         private val transferLogService: TransferLogService,
                         private val providerService: ProviderService,
                         @Value("\${adsSize}") val  adsSize: Int = 100) {

    @Post("/")
    fun postTransfer(@Body upload: Single<String>): Single<HttpResponse<TransferLogDTO>> {
        return upload.map {
            // TODO authorized provider access here
            val transferJson = dtoValidation.parseJson(it)
            val transferDTO = dtoValidation.parseToDTO(transferJson)
            if (transferDTO.ads.size>100 || transferDTO.ads.size<1) {
                throw ApiError("ads should be between 1 to max 100", ErrorType.INVALID_VALUE)
            }
            val providerDTO = providerService.findByUuid(transferDTO.provider.uuid)
            val md5 = it.md5Hex()
            if (transferLogService.existsByProviderIdAndMd5(providerDTO.id!!, md5)) {
                throw ApiError("Content already exists", ErrorType.CONFLICT)
            }
            transferDTO.provider.id = providerDTO.id
            HttpResponse.created(transferLogService.saveTransfer(transferDTO, md5, it))
        }
    }

    @Get("/{versionId}")
    fun getTransfer(@PathVariable versionId: Long): Single<HttpResponse<TransferLogDTO>> {
        return Single.just(HttpResponse.ok(transferLogService.findByVersionId(versionId)))
    }


}