package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO

@Controller("/api/v1/adstatus")
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