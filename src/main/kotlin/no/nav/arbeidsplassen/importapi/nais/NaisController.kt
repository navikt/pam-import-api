package no.nav.arbeidsplassen.importapi.nais

import io.javalin.Javalin
import io.javalin.http.HttpStatus
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import no.nav.arbeidsplassen.importapi.security.Roles


class NaisController(
    private val healthService: HealthService, private val prometheusMeterRegistry: PrometheusMeterRegistry
) {
    fun setupRoutes(javalin: Javalin) {
        javalin.get("/internal/isReady", { it.status(200) }, Roles.ROLE_UNPROTECTED)
        javalin.get(
            "/internal/isAlive",
            { if (healthService.isHealthy()) it.status(HttpStatus.OK) else it.status(HttpStatus.SERVICE_UNAVAILABLE) },
            Roles.ROLE_UNPROTECTED
        )
        javalin.get(
            "/internal/prometheus",
            { it.contentType(TextFormat.CONTENT_TYPE_004).result(prometheusMeterRegistry.scrape()) },
            Roles.ROLE_UNPROTECTED
        )
    }
}
