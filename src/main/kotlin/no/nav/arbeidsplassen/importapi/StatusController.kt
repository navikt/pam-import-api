package no.nav.arbeidsplassen.importapi

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.token.jwt.signature.secret.SecretSignatureConfiguration
import io.swagger.v3.oas.annotations.Hidden
import jakarta.annotation.security.PermitAll
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import org.slf4j.LoggerFactory

@PermitAll
@Controller("/internal")
@Hidden
class StatusController(
    private val secretSignatureConfiguration: SecretSignatureConfiguration,
    private val healthService: HealthService,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(StatusController::class.java)
    }

    @Get("/isReady", produces = [MediaType.TEXT_PLAIN])
    fun isReady(): HttpResponse<String> {
        if ("Thisisaverylongsecretandcanonlybeusedintest" == secretSignatureConfiguration.secret)
            return HttpResponse.serverError("Secret not set")
        return HttpResponse.ok("OK")
    }

    @Get("/isAlive", produces = [MediaType.TEXT_PLAIN])
    fun isAlive(): HttpResponse<String> {
        if (!healthService.isHealthy()) {
            LOG.error("A Kafka consumer is set to Error")
            return HttpResponse.serverError("Kafka consumer has error and stopped")
        }
        return HttpResponse.ok("OK")
    }


}
