package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import java.time.LocalDateTime
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/internal/adstates")
@Hidden
class AdStateFeedController(private val adStateService: AdStateService, private val adStateRepository: AdStateRepository) {

    @Get("/")
    fun getAdStates(@QueryValue updated: String, pageable: Pageable): Slice<AdStateDTO> {
        return adStateService.getAdStatesByUpdatedForInternalUse(LocalDateTime.parse(updated), pageable)
    }

}
