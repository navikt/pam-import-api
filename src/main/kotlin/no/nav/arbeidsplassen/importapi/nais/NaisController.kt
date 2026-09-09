package no.nav.arbeidsplassen.importapi.nais

import io.javalin.config.JavalinConfig
import io.javalin.http.ContentType
import io.javalin.http.HttpStatus
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.arbeidsplassen.importapi.config.JavalinController
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfigProperties
import org.slf4j.LoggerFactory

class NaisController(
    private val healthService: HealthService,
    private val prometheusMeterRegistry: PrometheusMeterRegistry,
    private val secretSignatureConfigProperties: SecretSignatureConfigProperties,
) : JavalinController {
    companion object {
        private val LOG = LoggerFactory.getLogger(NaisController::class.java)
    }

    override fun setupRoutes(javalin: JavalinConfig) {
        javalin.routes.get("/internal/isReady", {
            if ("Thisisaverylongsecretandcanonlybeusedintest" == secretSignatureConfigProperties.secret) {
                it
                    .contentType(ContentType.TEXT_PLAIN)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Secret not set")
            } else {
                it
                    .contentType(ContentType.TEXT_PLAIN)
                    .status(HttpStatus.OK)
                    .result("OK")
            }
        })
        javalin.routes.get(
            "/internal/isAlive",
            {
                if (healthService.isHealthy()) {
                    it
                        .contentType(ContentType.TEXT_PLAIN)
                        .status(HttpStatus.OK)
                        .result("OK")
                } else {
                    LOG.error("A Kafka consumer is set to Error")
                    it
                        .contentType(ContentType.TEXT_PLAIN)
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .result("Kafka consumer has error and stopped")
                }
            }
        )
        javalin.routes.get(
            "/internal/prometheus",
            {
                it
                    .contentType(ContentType.TEXT_PLAIN)
                    .result(prometheusMeterRegistry.scrape())
            }
        )
    }
}
