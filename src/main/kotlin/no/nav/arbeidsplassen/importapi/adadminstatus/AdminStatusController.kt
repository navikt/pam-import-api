package no.nav.arbeidsplassen.importapi.adadminstatus

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import io.javalin.openapi.OpenApiSecurity
import no.nav.arbeidsplassen.importapi.config.JavalinController
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class AdminStatusController(private val adminStatusService: AdminStatusService) : JavalinController {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusController::class.java)
        private fun Context.providerIdParam(): Long = pathParam("providerId").toLong()
        private fun Context.referenceParam(): String = pathParam("reference")
        private fun Context.versionIdParam(): Long = pathParam("versionId").toLong()
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    override fun setupRoutes(javalin: Javalin) {
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

    @OpenApi(
        path = "/stillingsimport/api/v1/adminstatus/{providerId}/{reference}",
        methods = [HttpMethod.GET],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "reference", type = String::class, required = true, description = "reference"),
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "adAdminStatus 200 response",
                content = [OpenApiContent(from = AdAdminStatusDTO::class)]
            ),
        ]
    )
    fun adAdminStatus(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val reference = ctx.referenceParam()
        val adAdminStatus = adminStatusService.findByProviderReference(providerId, reference)
        ctx.status(HttpStatus.OK).json(adAdminStatus)
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/adminstatus/{providerId}/versions/{versionId}",
        methods = [HttpMethod.GET],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "versionId", type = Long::class, required = true, description = "versionId"),
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "adAdminStatusByVersion 200 response",
                content = [OpenApiContent(from = Array<AdAdminStatusDTO>::class)]
            ),
        ]
    )
    fun adAdminStatusByVersion(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val versionId = ctx.versionIdParam()
        val adAdminStatusList = adminStatusService.findByVersionAndProviderId(versionId, providerId)
        ctx.status(HttpStatus.OK).json(adAdminStatusList)
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/adminstatus/{providerId}/uuid/{uuid}",
        methods = [HttpMethod.GET],
        security = [OpenApiSecurity(name = "BearerAuth")],
        pathParams = [
            OpenApiParam(name = "providerId", type = Long::class, required = true, description = "providerId"),
            OpenApiParam(name = "uuid", type = String::class, required = true, description = "uuid"),
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "adAdminStatusByUuid 200 response",
                content = [OpenApiContent(from = AdAdminStatusDTO::class)]
            ),
        ]
    )
    fun adAdminStatusByUuid(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val uuid = ctx.uuidParam()
        val adAdminStatus = adminStatusService.findByUuid(uuid)
        ctx.status(HttpStatus.OK).json(adAdminStatus)
    }
}
