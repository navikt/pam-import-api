package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.ApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes i tester
 */
class LocalApplicationContext(
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
                localEnv["DB_HOST"] = localConfig.host
                localEnv["DB_PORT"] = localConfig.getMappedPort(5432).toString()
            }

    private val kafkaContainer: ConfluentKafkaContainer = ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.0")
        .waitingFor(Wait.forListeningPort())
        .withReuse(false)
        .apply {
            start()
        }
        .also { localConfig ->
            localEnv["KAFKA_BROKERS"] = localConfig.bootstrapServers
        }

    //val applicationProperties = testApplicationProperties(dbHost, dbPort, kafkaBrokers)
    val applicationContext = object : ApplicationContext(localApplicationProperties(localEnv)) {
        override val outgoingPortsApplicationContext: OutgoingPortsApplicationContext =
            TestOutgoingPortsApplicationContext()
    }

    companion object {
        fun localApplicationProperties(dbHost: String, dbPort: String, kafkaBrokers: String) = ApplicationProperties(
            secretSignatureConfigProperties = TestSecretSignatureConfigProperties(),
            databaseConfigProperties = TestDatabaseConfigProperties(dbHost, dbPort),
            kafkaConfigProperties = TestKafkaConfigProperties(kafkaBrokers),
            outgoingPortsConfigProperties = TestOutgoingPortsConfigProperties(),
            servicesConfigProperties = TestServicesConfigProperties(adminStatusSyncKafkaEnabled = true),
            controllerConfigProperties = TestControllerConfigProperties(),
            schedulerConfigProperties = TestSchedulerConfigProperties(
                adOutboxJobEnabled = true,
                transferlogJobEnabled = true
            )
        )

        fun localApplicationProperties(env: Map<String, String>) = localApplicationProperties(
            dbHost = env.variable("DB_HOST"),
            dbPort = env.variable("DB_PORT"),
            kafkaBrokers = env.variable("KAFKA_BROKERS")
        )
    }
}
