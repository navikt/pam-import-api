package no.nav.arbeidsplassen.importapi.adstate

import io.javalin.Javalin
import io.javalin.http.Context
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import org.slf4j.LoggerFactory

// TODO @Hidden
class AdPreviewController(
    private val adStateService: AdStateService,
    private val previewUrl: String // @Value("\${ad.preview.url}")
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPreviewController::class.java)
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/api/v1/preview/{uuid}",
            { previewAd(it.uuidParam()) },
        )

        javalin.get(
            "/frontend/{uuid}",
            { it.redirect(forwardIndexHtml(it.uuidParam())) },
        )
    }

    // Fixme: HPH Denne ser ikke ut til å være i bruk basert på accessloggene
    fun previewAd(uuid: String): AdDTO {
        LOG.info("Previewing ad as json. Uuid:  {}", uuid)
        return adStateService.getAdStateByUuid(uuid).ad
    }

    fun forwardIndexHtml(uuid: String): String {
        LOG.info("Previewing ad. Redirecting to {} for uuid: {}", previewUrl, uuid)
        return "$previewUrl/$uuid"
    }
}
