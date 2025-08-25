package no.nav.arbeidsplassen.importapi.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.metrics.prometheus.PrometheusMetricsTrackerFactory
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.JdbcAdminStatusRepository
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxRepository
import no.nav.arbeidsplassen.importapi.adoutbox.JdbcAdOutboxRepository
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsRepository
import no.nav.arbeidsplassen.importapi.adpuls.JdbcAdPulsRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.adstate.JdbcAdStateRepository
import no.nav.arbeidsplassen.importapi.provider.JdbcProviderRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.transferlog.JdbcTransferLogRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository

class DatabaseApplicationContext(
    private val databaseConfigProperties: DatabaseConfigProperties,
    private val baseServicesApplicationContext: BaseServicesApplicationContext,
) {
    val dataSource = HikariConfig().apply {
        jdbcUrl =
            "jdbc:postgresql://${databaseConfigProperties.host}:${databaseConfigProperties.port}/${databaseConfigProperties.database}${databaseConfigProperties.additionalParameter}"
        minimumIdle = 1
        maximumPoolSize = 8
        driverClassName = databaseConfigProperties.dbDriver
        poolName = "default"
        initializationFailTimeout = 5000
        username = databaseConfigProperties.user
        password = databaseConfigProperties.pw
        // CollectorRegistry.defaultRegistry
        metricsTrackerFactory =
            PrometheusMetricsTrackerFactory(baseServicesApplicationContext.prometheusRegistry.prometheusRegistry)
        validate()
    }.let(::HikariDataSource)

    val txTemplate = TxTemplate(dataSource)

    val providerRepository: ProviderRepository = JdbcProviderRepository(txTemplate)
    val transferLogRepository: TransferLogRepository = JdbcTransferLogRepository(txTemplate)
    val adStateRepository: AdStateRepository = JdbcAdStateRepository(txTemplate)
    val adminStatusRepository: AdminStatusRepository = JdbcAdminStatusRepository(txTemplate)
    val adPulsRepository: AdPulsRepository = JdbcAdPulsRepository(txTemplate)
    val adOutboxRepository: AdOutboxRepository = JdbcAdOutboxRepository(txTemplate)
}
