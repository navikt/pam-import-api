package no.nav.arbeidsplassen.importapi.adstate

import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.config.JavalinController
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class AdStateInternalController(private val adStateService: AdStateService) : JavalinController {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdStateInternalController::class.java)
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    override fun setupRoutes(javalin: JavalinConfig) {
        javalin.routes.get(
            "/internal/adstates/{uuid}",
            { getAdState(it) },
            Roles.ROLE_ADMIN
        )

        javalin.routes.put(
            "/internal/adstates/{uuid}/resend",
            { resendAdState(it) },
            Roles.ROLE_ADMIN
        )
    }

    fun getAdState(ctx: Context) {
        val uuid: String = ctx.uuidParam()
        val adState = adStateService.getAdStateByUuid(uuid)
        ctx.status(HttpStatus.OK).json(adState)
    }

    fun resendAdState(ctx: Context) {
        val uuid: String = ctx.uuidParam()
        LOG.info("Resend adstate $uuid")
        val adState = adStateService.resendAdState(uuid)
        ctx.status(HttpStatus.OK).json(adState)
    }

}
