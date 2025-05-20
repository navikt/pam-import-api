package no.nav.arbeidsplassen.importapi.adadminstatus

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
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
            { adAdminStatus(it) },
            Roles.ROLE_PROVIDER,
            Roles.ROLE_ADMIN
        )
        javalin.get(
            "/api/v1/adminstatus/{providerId}/versions/{versionId}",
            { adAdminStatusByVersion(it) },
            Roles.ROLE_PROVIDER,
            Roles.ROLE_ADMIN
        )
        javalin.get(
            "/api/v1/adminstatus/{providerId}/uuid/{uuid}",
            { adAdminStatusByUuid(it) },
            Roles.ROLE_PROVIDER,
            Roles.ROLE_ADMIN
        )
    }

    fun adAdminStatus(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val reference = ctx.referenceParam()
        val adAdminStatus = adminStatusService.findByProviderReference(providerId, reference)
        ctx.status(HttpStatus.OK).json(adAdminStatus)
    }

    fun adAdminStatusByVersion(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val versionId = ctx.versionIdParam()
        val adAdminStatusList = adminStatusService.findByVersionAndProviderId(versionId, providerId)
        ctx.status(HttpStatus.OK).json(adAdminStatusList)
    }

    fun adAdminStatusByUuid(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val uuid = ctx.uuidParam()
        val adAdminStatus = adminStatusService.findByUuid(uuid)
        ctx.status(HttpStatus.OK).json(adAdminStatus)
    }
}
