package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.core.convert.format.Format
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.*
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import java.time.LocalDateTime
import java.util.*


@Controller("/api/v1/adstates")
class AdStateController(private val adStateService: AdStateService) {


    @Get("/{providerId}/{reference}")
    fun getAdStateByProviderReference(@PathVariable providerId:Long, @PathVariable reference:String): AdStateDTO
            = adStateService.getAdStatesByProviderReference(providerId, reference)

    @Get("/{uuid}")
    fun getAdStateByUuid(@PathVariable uuid: UUID): AdStateDTO
            = adStateService.getAdStateByUuid(uuid)

    @Get("/versions/{versionId}")
    fun getAdStatesByProvider(@PathVariable versionId: Long, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByVersionId(versionId, pageable)
    }

    @Get("/")
    fun getAdStates(@QueryValue @Format("yyyy-MM-dd'T'HH:mm:ss.SSS") updated: LocalDateTime, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByUpdated(updated, pageable)
    }

}