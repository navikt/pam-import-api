package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import java.util.*
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/preview")
@Hidden
class AdPreviewController(private val adStateService: AdStateService) {

    @Get("/{uuid}")
    fun previewAd(@PathVariable uuid: String): AdDTO {
        return adStateService.getAdStateByUuid(uuid).ad
    }

}
