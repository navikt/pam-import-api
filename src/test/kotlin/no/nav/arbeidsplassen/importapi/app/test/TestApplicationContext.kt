package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.ApplicationContext
import no.nav.arbeidsplassen.importapi.config.ApplicationProperties
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.config.TestControllerConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestDatabaseConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestKafkaConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestOutgoingPortsConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestSchedulerConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestSecretSignatureConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestServicesConfigProperties
import no.nav.arbeidsplassen.importapi.config.variable

/*
 * Application context som kan brukes i tester
 */
class TestApplicationContext(applicationProperties: ApplicationProperties) {
    //val applicationProperties = testApplicationProperties(dbHost, dbPort, kafkaBrokers)
    val applicationContext = object : ApplicationContext(applicationProperties) {
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
