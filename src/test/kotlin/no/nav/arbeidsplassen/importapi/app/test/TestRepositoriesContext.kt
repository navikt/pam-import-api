package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.config.BaseServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.DatabaseApplicationContext
import no.nav.arbeidsplassen.importapi.config.SecurityServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.TestDatabaseConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestSecretSignatureConfigProperties
import no.nav.arbeidsplassen.importapi.config.variable
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes i tester
 */
class TestRepositoriesContext(
    private val localEnv: MutableMap<String, String>,
) {
    private val postgresContainer: PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .waitingFor(Wait.forListeningPort())
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .apply {
                start()
            }
            .also { localConfig ->
                // localEnv["DB_URL"] = localConfig.jdbcUrl.addInitScript()
                localEnv["DB_HOST"] = localConfig.host
                localEnv["DB_PORT"] = localConfig.getMappedPort(5432).toString()
                // localEnv["DB_ADDITIONAL_PARAMETER"] = "?TC_INITSCRIPT=postgres/postgres-init.sql"
            }

    val secretSignatureConfigProperties = TestSecretSignatureConfigProperties()
    val databaseConfigurationProperties = TestDatabaseConfigProperties(
        host = localEnv.variable("DB_HOST"),
        port = localEnv.variable("DB_PORT"),
    )
    val baseServicesApplicationContext = BaseServicesApplicationContext()
    val databaseApplicationContext = DatabaseApplicationContext(
        databaseConfigProperties = databaseConfigurationProperties,
        baseServicesApplicationContext = baseServicesApplicationContext
    )
    val securityServicesApplicationContext = SecurityServicesApplicationContext(
        secretSignatureConfigProperties = secretSignatureConfigProperties,
        databaseApplicationContext = databaseApplicationContext
    )
}
