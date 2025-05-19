package no.nav.arbeidsplassen.importapi.adadminstatus

import io.javalin.Javalin
import io.javalin.http.Context
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class AdminStatusController(private val adminStatusService: AdminStatusService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusController::class.java)
        private fun Context.providerIdParam(): Long = pathParam("providerId").toLong()
        private fun Context.referenceParam(): String = pathParam("reference")
        private fun Context.versionIdParam(): Long = pathParam("versionId").toLong()
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/api/v1/adminstatus/{providerId}/{reference}",
            { adAdminStatus(it.providerIdParam(), it.referenceParam()) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )

        javalin.get(
            "/api/v1/adminstatus/{providerId}/versions/{versionId}",
            { adAdminStatusByVersion(it.versionIdParam(), it.providerIdParam()) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )

        javalin.get(
            "/api/v1/adminstatus/{providerId}/uuid/{uuid}",
            { adAdminStatusByUuid(it.providerIdParam(), it.uuidParam()) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
    }

    fun adAdminStatus(providerId: Long, reference: String): AdAdminStatusDTO {
        return adminStatusService.findByProviderReference(providerId, reference)
    }

    fun adAdminStatusByVersion(versionId: Long, providerId: Long): List<AdAdminStatusDTO> {
        return adminStatusService.findByVersionAndProviderId(versionId, providerId)
    }

    fun adAdminStatusByUuid(providerId: Long, uuid: String): AdAdminStatusDTO {
        return adminStatusService.findByUuid(uuid)
    }
}
