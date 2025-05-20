package no.nav.arbeidsplassen.importapi.transferlog

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

// TODO @Hidden
class TransferlogInternalController(private val transferLogService: TransferLogService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferlogInternalController::class.java)

        private fun Context.versionIdParam(): Long = pathParam("versionId").toLong()
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/internal/transfers/{versionId}",
            { getByTransferId(it) },
            Roles.ROLE_ADMIN
        )
        javalin.put(
            "/internal/transfers/{versionId}/resend",
            { resendTransfer(it) },
            Roles.ROLE_ADMIN
        )
    }

    fun getByTransferId(ctx: Context) {
        val versionId = ctx.versionIdParam()
        val transferLog = transferLogService.findByVersionId(versionId)
        ctx.status(HttpStatus.OK).json(transferLog)
    }

    fun resendTransfer(ctx: Context) {
        val versionId = ctx.versionIdParam()
        LOG.info("resend $versionId by admin")
        val transferLog = transferLogService.resend(versionId)
        ctx.status(HttpStatus.OK).json(transferLog)
    }
}
