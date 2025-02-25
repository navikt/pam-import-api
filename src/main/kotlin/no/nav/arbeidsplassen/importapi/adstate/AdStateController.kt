package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles

@ProviderAllowed(value = [Roles.ROLE_PROVIDER,Roles.ROLE_ADMIN])
@Controller("/api/v1/adstates")
@SecurityRequirement(name = "bearer-auth")
class AdStateController(private val adStateService: AdStateService) {

    @Get("/{providerId}/{reference}")
    fun getAdStateByProviderReference(@PathVariable providerId:Long, @PathVariable reference:String): AdStatePublicDTO
            = adStateService.getAdStatesByProviderReference(providerId, reference)

    @Get("/{providerId}/uuid/{uuid}")
    fun getAdStateByUuid(@PathVariable providerId: Long, @PathVariable uuid: String): AdStatePublicDTO
            = adStateService.getAdStateByUuidAndProviderId(uuid, providerId)

    /*
    // HPH: Kommenterer ut denne da jeg ikke finner at den er i bruk i accessloggene
    @Get("/{providerId}/versions/{versionId}")
    fun getAdStatesByProvider(@PathVariable providerId: Long, @PathVariable versionId: Long, pageable: Pageable): Slice<AdStatePublicDTO> {
        return adStateService.getAdStatesByVersionIdAndProviderId(versionId, providerId, pageable)
    }
    */

}
