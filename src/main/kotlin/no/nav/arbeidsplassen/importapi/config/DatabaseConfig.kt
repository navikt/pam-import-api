package no.nav.arbeidsplassen.importapi.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory
import io.prometheus.client.CollectorRegistry

class DatabaseConfig(env: Map<String, String>,
                     private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry) {
    private val host = env.variable("DB_HOST")
    private val port = env.variable("DB_PORT")
    private val database = env.variable("DB_DATABASE")
    private val user = env.variable("DB_USERNAME")
    private val pw = env.variable("DB_PASSWORD")
    private val dbDriver = env.variable("DB_DRIVER")
    // private val url = env.variable("DB_URL")
    private val additionalParameter = env["DB_ADDITIONAL_PARAMETER"] ?: ""

    fun lagDatasource() = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://$host:$port/$database$additionalParameter"
        minimumIdle = 1
        maximumPoolSize = 8
        driverClassName = dbDriver
        poolName = "default"
        initializationFailTimeout = 5000
        username = user
        password = pw
        metricsTrackerFactory = PrometheusMetricsTrackerFactory(collectorRegistry)
        validate()
    }.let(::HikariDataSource)

}

fun Map<String, String>.variable(felt: String) = this[felt] ?: error("$felt er ikke angitt")
