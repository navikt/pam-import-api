package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.ApplicationContext
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes i tester
 */
/*
 * Application context som kan brukes i tester
 */
class TestApplicationContext(
    private val env: MutableMap<String, String>,
    val localPostgres: Any = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
        .waitingFor(Wait.forListeningPort())
        .apply { start() }
        .also { localConfig ->
            // localEnv["DB_URL"] = localConfig.jdbcUrl.addInitScript()
            env["DB_HOST"] = localConfig.host
            env["DB_PORT"] = localConfig.getMappedPort(5432).toString()
            // localEnv["DB_ADDITIONAL_PARAMETER"] = "?TC_INITSCRIPT=postgres/postgres-init.sql"
        }
) : ApplicationContext(env) {

    override val outgoingPortsApplicationContext: OutgoingPortsApplicationContext =
        TestOutgoingPortsApplicationContext()


}
