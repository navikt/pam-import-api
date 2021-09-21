package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.server.types.files.StreamedFile
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.security.PermitAll

@PermitAll
@Controller
@Hidden
class AdPreviewController(private val adStateService: AdStateService, private val environment: Environment) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPreviewController::class.java)
    }

    @Get("/api/v1/preview/{uuid}")
    fun previewAd(@PathVariable uuid: String): AdDTO {
        return adStateService.getAdStateByUuid(uuid).ad
    }

    @Get("/api/v1/preview/{providerId}/{reference}")
    fun previewAd(@PathVariable providerId: Long, @PathVariable reference: String): AdDTO {
        return adStateService.getAdStatesByProviderReference(providerId, reference).ad
    }

    @Get("/frontend{/path:.*}")
    fun forwardIndexHtml(path:String?) : Optional<StreamedFile> {
        LOG.info("Got frontend request for path {}",path)
        return environment.getResource("classpath:frontend/index.html").map { StreamedFile(it) }
    }

}
