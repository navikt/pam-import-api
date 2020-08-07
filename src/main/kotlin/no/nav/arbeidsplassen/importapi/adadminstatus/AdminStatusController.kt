package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/adminstatus")
@SecurityRequirement(name = "bearer-auth")
class AdminStatusController(private val adminStatusService: AdminStatusService) {

    @Get("/{providerId}/{reference}")
    fun adAdminStatus(@PathVariable providerId:Long , @PathVariable reference: String): AdAdminStatusDTO {
        return adminStatusService.findByProviderReference(providerId, reference)
    }

    @Get("/{providerId}/versions/{versionId}")
    fun adAdminStatusByVersion(@PathVariable versionId: Long, @PathVariable providerId: Long): List<AdAdminStatusDTO> {
        return adminStatusService.findByVersionAndProviderId(versionId, providerId)
    }
}

