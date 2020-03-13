package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import javax.annotation.security.RolesAllowed

@RolesAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/adminstatus")
class AdminStatusController(private val adminStatusService: AdminStatusService) {

    @Get("/{providerId}/{reference}")
    fun adAdminStatus(@PathVariable providerId:Long , @PathVariable reference: String): AdAdminStatusDTO {
        return adminStatusService.findByProviderReference(providerId, reference)
    }

    @Get("/versions/{versionId}")
    fun adAdminStatusByVersion(@PathVariable versionId: Long): List<AdAdminStatusDTO> {
        return adminStatusService.findByVersion(versionId)
    }
}

