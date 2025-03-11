package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.context.annotation.Value
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.server.types.files.StreamedFile
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.util.*
import jakarta.annotation.security.PermitAll

@PermitAll
@Controller
@Hidden
class AdPreviewController(private val adStateService: AdStateService, private val environment: Environment, @Value("\${ad.preview.url}") private val previewUrl: String) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPreviewController::class.java)
    }

    // Fixme: HPH Denne ser ikke ut til å være i bruk basert på accessloggene
    @Get("/api/v1/preview/{uuid}")
    fun previewAd(@PathVariable uuid: String): AdDTO {
        LOG.info("Previewing ad as json. Uuid:  {}", uuid)
        return adStateService.getAdStateByUuid(uuid).ad
    }

    @Get("/frontend/{uuid}")
    fun forwardIndexHtml(uuid: String) : HttpResponse<URI> {
        LOG.info("Previewing ad. Redirecting to {} for uuid: {}", previewUrl, uuid)
        return HttpResponse.redirect(URI("$previewUrl/$uuid"))
    }
}
