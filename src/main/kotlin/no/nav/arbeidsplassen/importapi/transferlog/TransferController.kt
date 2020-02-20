package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.dto.ApiError
import no.nav.arbeidsplassen.importapi.md5Hex
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import java.util.*


@Controller("/api/v1/transfers")
class TransferController(private val dtoValidation: DTOValidation,
                         private val transferLogService: TransferLogService,
                         private val providerService: ProviderService) {

    @Post("/")
    fun postTransfer(@Body upload: Single<String>): Single<HttpResponse<TransferLogDTO>> {
        return upload.map {
            // TODO authorized provider access here
            val transferJson = dtoValidation.parseJson(it)
            val transferDTO = dtoValidation.parseToDTO(transferJson)
            val providerDTO = checkProviderExistElseThrow(transferDTO.provider.uuid)
            val md5 = it.md5Hex()
            if (transferLogService.existsByProviderIdAndMd5(providerDTO.id!!, md5))
                throw ApiError("Content already exists", ErrorType.CONFLICT)
            transferDTO.provider.id = providerDTO.id
            HttpResponse.created(transferLogService.saveTransfer(transferDTO, md5, it))
        }
    }

    @Get("/{versionId}")
    fun getTransfer(@PathVariable versionId: Long): Single<HttpResponse<TransferLogDTO>> {
        // TODO authorized here
        return try {
            Single.just(HttpResponse.ok(transferLogService.findByVersionId(versionId)))
        }
        catch (e: NoSuchElementException) {
            throw ApiError("Transfer $versionId does not exist", ErrorType.NOT_FOUND)
        }
    }



    private fun checkProviderExistElseThrow(uuid: UUID): ProviderDTO {
        try {
            return  providerService.findByUuid(uuid)
        } catch (e: NoSuchElementException) {
            throw ApiError("Provider $uuid does not exist", ErrorType.NOT_FOUND)
        }
    }

}