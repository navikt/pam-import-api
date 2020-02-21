package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.dto.ApiError
import no.nav.arbeidsplassen.importapi.dto.ErrorType
import java.time.LocalDateTime

@Controller("/api/v1/adstates")
class AdStateController(private val adStateService: AdStateService) {


    @Get("/{providerId}/{reference}")
    fun getAdStateByProviderReference(@PathVariable providerId:Long, @PathVariable reference:String): AdStateDTO {
        return try {
            adStateService.getAdStatesByProviderReference(providerId, reference)
        }
        catch (e: NoSuchElementException) {
            throw ApiError("Adstate for $providerId $reference does not exist", ErrorType.NOT_FOUND)
        }
    }

    @Get("/versions/{versionId}")
    fun getAdStatesByProvider(@PathVariable versionId: Long, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByVersionId(versionId, pageable)
    }

    @Get("/")
    fun getAdStates(@QueryValue updated: LocalDateTime, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByUpdated(updated, pageable)
    }
}