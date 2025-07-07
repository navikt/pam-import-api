package no.nav.arbeidsplassen.importapi.nais

import io.javalin.Javalin
import io.javalin.http.ContentType
import io.javalin.http.HttpStatus
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeidsplassen.importapi.config.JavalinController
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfigProperties
import org.slf4j.LoggerFactory

// @Hidden
class NaisController(
    private val healthService: HealthService,
    private val prometheusMeterRegistry: PrometheusMeterRegistry,
    private val secretSignatureConfigProperties: SecretSignatureConfigProperties,
) : JavalinController {
    companion object {
        private val LOG = LoggerFactory.getLogger(NaisController::class.java)
    }

    override fun setupRoutes(javalin: Javalin) {
        javalin.get("/internal/isReady", {
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
        javalin.get(
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
        javalin.get(
            "/internal/prometheus",
            {
                it
                    .contentType(TextFormat.CONTENT_TYPE_004)
                    .result(prometheusMeterRegistry.scrape())
            }
        )
    }
}
