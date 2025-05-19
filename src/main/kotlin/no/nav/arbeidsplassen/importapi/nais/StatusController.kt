package no.nav.arbeidsplassen.importapi.nais

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
