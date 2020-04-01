package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import java.time.LocalDateTime
import javax.annotation.security.RolesAllowed

@ProviderAllowed(value = [Roles.ROLE_ADMIN])
@Controller("/api/v1/adstates")
class AdStateController(private val adStateService: AdStateService) {

    @Get("/{providerId}/{reference}")
    fun getAdStateByProviderReference(@PathVariable providerId:Long, @PathVariable reference:String): AdStateDTO
            = adStateService.getAdStatesByProviderReference(providerId, reference)

    @Get("/{providerId}/uuid/{uuid}")
    fun getAdStateByUuid(@PathVariable providerId: Long, @PathVariable uuid: String): AdStateDTO
            = adStateService.getAdStateByUuidAndProviderId(uuid, providerId)

    @Get("/{providerId}/versions/{versionId}")
    fun getAdStatesByProvider(@PathVariable providerId: Long, @PathVariable versionId: Long, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByVersionIdAndProviderId(versionId, providerId, pageable)
    }
}