package no.nav.arbeidsplassen.importapi.adstate

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.config.JavalinController
import org.slf4j.LoggerFactory

class AdPreviewController(
    private val adStateService: AdStateService,
    private val previewUrl: String // @Value("\${ad.preview.url}")
) : JavalinController {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPreviewController::class.java)
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    override fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/preview/{uuid}", { previewAd(it) })
        javalin.get("/frontend/{uuid}", { forwardIndexHtml(it) })
    }

    // Fixme: HPH Denne ser ikke ut til å være i bruk basert på accessloggene
    fun previewAd(ctx: Context) {
        val uuid: String = ctx.uuidParam()
        LOG.info("Previewing ad as json. Uuid:  {}", uuid)
        ctx.status(HttpStatus.OK).json(adStateService.getAdStateByUuid(uuid).ad)
    }

    fun forwardIndexHtml(ctx: Context) {
        val uuid: String = ctx.uuidParam()
        LOG.info("Previewing ad. Redirecting to {} for uuid: {}", previewUrl, uuid)
        ctx.redirect("$previewUrl/$uuid")
    }
}
