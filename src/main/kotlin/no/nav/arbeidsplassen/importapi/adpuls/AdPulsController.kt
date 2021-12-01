package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/stats/")
@SecurityRequirement(name = "bearer-auth")
class AdPulsController(private val adPulsService: AdPulsService) {

    @Get("/{providerId}/{reference}")
    fun getAllStatsForProviderReference(@PathVariable providerId: Long, @PathVariable reference: String): List<AdPulsDTO> {
        return adPulsService.findByProviderReference(providerId, reference)
    }

    @Get("/providerId")
    fun getAllTodayStatsForProvider(@PathVariable providerId: Long):List<AdPulsDTO> {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        return adPulsService.findByProviderIdAndUpdatedAfter(providerId, today)
    }

}
