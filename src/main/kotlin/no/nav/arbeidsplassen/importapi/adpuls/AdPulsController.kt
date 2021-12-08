package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.Sort.Order.Direction
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/stats/")
@SecurityRequirement(name = "bearer-auth")
class AdPulsController(private val adPulsService: AdPulsService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsController::class.java)
    }

    @Get("/{providerId}")
    fun getAllTodayStatsForProvider(@PathVariable providerId: Long, @QueryValue from: String, pageable: Pageable):Slice<AdPulsDTO> {
        val fromDate = LocalDateTime.parse(from).truncatedTo(ChronoUnit.HOURS)
        LOG.info("Getting stats for provider $providerId from $fromDate")
        require(fromDate.isAfter(LocalDateTime.now().minusHours(24))) { "date is out of range, max 24h from now"}
        require(pageable.size<=1000) {"size can not be more than 1000"}
        return adPulsService.findByProviderIdAndUpdatedAfter(providerId, fromDate, pageable)
    }

}
