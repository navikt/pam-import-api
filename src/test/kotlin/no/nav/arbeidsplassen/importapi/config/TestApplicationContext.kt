package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.ApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/*
 * Application context som kan brukes i tester
 */
class TestApplicationContext(
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

    private val kafkaContainer: KafkaContainer =
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka")) //apache/kafka-native:3.8.1
            .waitingFor(Wait.forListeningPort())
            .withReuse(false)
            .apply {
                start()
            }
            .also { localConfig ->
                localEnv["KAFKA_BROKERS"] = localConfig.bootstrapServers
            }

    val applicationContext = object : ApplicationContext(testApplicationProperties(localEnv)) {
        override val outgoingPortsApplicationContext: OutgoingPortsApplicationContext =
            TestOutgoingPortsApplicationContext()
    }

    companion object {
        fun testApplicationProperties(dbHost: String, dbPort: String, kafkaBrokers: String) = ApplicationProperties(
            secretSignatureConfigProperties = TestSecretSignatureConfigProperties(),
            databaseConfigProperties = TestDatabaseConfigProperties(dbHost, dbPort),
            kafkaConfigProperties = TestKafkaConfigProperties(kafkaBrokers),
            outgoingPortsConfigProperties = TestOutgoingPortsConfigProperties(),
            servicesConfigProperties = TestServicesConfigProperties(),
            controllerConfigProperties = TestControllerConfigProperties(),
            schedulerConfigProperties = TestSchedulerConfigProperties()
        )

        fun testApplicationProperties(env: Map<String, String>) = testApplicationProperties(
            dbHost = env.variable("DB_HOST"),
            dbPort = env.variable("DB_PORT"),
            kafkaBrokers = env.variable("KAFKA_BROKERS")
        )
    }
}
