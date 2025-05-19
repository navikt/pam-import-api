package no.nav.arbeidsplassen.importapi.adstate

import io.javalin.Javalin
import io.javalin.http.Context
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory


// TODO @Hidden
class AdStateInternalController(private val adStateService: AdStateService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdStateInternalController::class.java)
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/internal/adstates/{uuid}",
            { getAdState(it.uuidParam()) },
            Roles.ROLE_ADMIN
        )

        javalin.put(
            "/internal/adstates/{uuid}/resend",
            { resendAdState(it.uuidParam()) },
            Roles.ROLE_ADMIN
        )
    }

    fun getAdState(uuid: String): AdStatePublicDTO {
        return adStateService.getAdStateByUuid(uuid)
    }

    fun resendAdState(uuid: String): AdStatePublicDTO {
        LOG.info("Resend adstate $uuid")
        return adStateService.resendAdState(uuid)
    }

}
