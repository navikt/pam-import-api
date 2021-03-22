package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory


@ProviderAllowed(value = [Roles.ROLE_ADMIN])
@Controller("/internal/adstates")
@Hidden
class AdStateInternalController(private val adStateService: AdStateService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdStateInternalController::class.java)
    }

    @Get("/{uuid}")
    fun getAdState(@PathVariable uuid: String): AdStatePublicDTO {
        return adStateService.getAdStateByUuid(uuid)
    }

    @Put("/{uuid}/resend")
    fun resendAdState(@PathVariable uuid: String): AdStatePublicDTO {
        LOG.info("Resend adstate $uuid")
        return adStateService.resendAdState(uuid)
    }

}
