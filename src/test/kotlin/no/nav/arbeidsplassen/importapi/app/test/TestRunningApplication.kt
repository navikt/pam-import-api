package no.nav.arbeidsplassen.importapi.app.test

import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import no.nav.arbeidsplassen.importapi.ApplicationContext
import no.nav.arbeidsplassen.importapi.kjørFlywayMigreringer
import no.nav.arbeidsplassen.importapi.startApp
import no.nav.arbeidsplassen.importapi.startJavalin
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

abstract class TestRunningApplication {

    companion object {
        const val lokalUrlBase = "http://localhost:9028"

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
                    dbHost = localConfig.host
                    dbPort = localConfig.getMappedPort(5432).toString()
                    // env["DB_HOST"] = localConfig.host
                    // env["DB_PORT"] = localConfig.getMappedPort(5432).toString()
                    // localEnv["DB_ADDITIONAL_PARAMETER"] = "?TC_INITSCRIPT=postgres/postgres-init.sql"
                }

        private val kafkaContainer: KafkaContainer =
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
                .waitingFor(Wait.forListeningPort())
                .withReuse(false)
                .apply {
                    start()
                }
                .also { localConfig ->
                    kafkaBrokers = localConfig.bootstrapServers
                    // env["KAFKA_BROKERS"] = localConfig.bootstrapServers
                }

        private val dbHost: String
        private val dbPort: String
        private val kafkaBrokers: String

        @JvmStatic
        val appCtx: ApplicationContext =
            TestApplicationContext(
                TestApplicationContext.testApplicationProperties(
                    dbHost,
                    dbPort,
                    kafkaBrokers
                )
            ).applicationContext

        @JvmStatic
        val javalin: Javalin = appCtx.startApp()

        @BeforeAll
        @JvmStatic
        fun setup() {

        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            javalin.stop()
            postgresContainer.stop()
            kafkaContainer.stop()
        }
    }
}

abstract class TestRepositories {

    companion object {
        const val lokalUrlBase = "http://localhost:9028"

        private val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
                .waitingFor(Wait.forListeningPort())
                .withReuse(false)
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test")
                .apply {
                    start()
                }
                .also { localConfig ->
                    // localEnv["DB_URL"] = localConfig.jdbcUrl.addInitScript()
                    dbHost = localConfig.host
                    dbPort = localConfig.getMappedPort(5432).toString()
                    // env["DB_HOST"] = localConfig.host
                    // env["DB_PORT"] = localConfig.getMappedPort(5432).toString()
                    // localEnv["DB_ADDITIONAL_PARAMETER"] = "?TC_INITSCRIPT=postgres/postgres-init.sql"
                }

        private val dbHost: String
        private val dbPort: String

        @JvmStatic
        val appCtx: TestRepositoriesContext = TestRepositoriesContext(dbHost, dbPort)

        @JvmStatic
        val javalin: Javalin = appCtx.startApp()

        @BeforeAll
        @JvmStatic
        fun setup() {

        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            javalin.stop()
            postgresContainer.stop()
        }

        private fun TestRepositoriesContext.startApp(): Javalin {

            kjørFlywayMigreringer(this.databaseApplicationContext.dataSource)

            val javalin = startJavalin(
                port = 8080,
                jsonMapper = JavalinJackson(this.baseServicesApplicationContext.objectMapper),
                meterRegistry = this.baseServicesApplicationContext.prometheusRegistry,
                accessManager = this.securityServicesApplicationContext.accessManager,
                onServerStartedListeners = emptyList(),
                onServerStoppingListeners = emptyList()
            )
            return javalin
        }
    }
}
