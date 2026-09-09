package no.nav.arbeidsplassen.importapi.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NaisApplicationPropertiesTest {
    @Test
    fun `database configuration uses Nais variables without a JDBC URL alias`() {
        val properties = NaisDatabaseConfigProperties(
            mapOf(
                "DB_HOST" to "database",
                "DB_PORT" to "5433",
                "DB_DATABASE" to "importapi",
                "DB_USERNAME" to "importapi",
                "DB_PASSWORD" to "test-password",
                "DB_DRIVER" to "org.postgresql.Driver"
            )
        )

        assertEquals("database", properties.host)
        assertEquals("5433", properties.port)
        assertEquals("importapi", properties.database)
        assertEquals("importapi", properties.user)
        assertEquals("test-password", properties.pw)
        assertEquals("org.postgresql.Driver", properties.dbDriver)
        assertEquals("", properties.additionalParameter)
    }

    @Test
    fun `Kafka configuration uses Nais variables without SSL aliases`() {
        val properties = NaisKafkaConfigProperties(
            mapOf(
                "KAFKA_BROKERS" to "broker:9093",
                "KAFKA_CREDSTORE_PASSWORD" to "test-password",
                "KAFKA_TRUSTSTORE_PATH" to "/var/run/secrets/kafka/client.truststore.jks",
                "KAFKA_KEYSTORE_PATH" to "/var/run/secrets/kafka/client.keystore.p12",
                "NAIS_APP_NAME" to "pam-import-api"
            )
        )

        assertEquals("broker:9093", properties.brokers)
        assertEquals("test-password", properties.credstorePassword)
        assertEquals("/var/run/secrets/kafka/client.truststore.jks", properties.truststorePath)
        assertEquals("/var/run/secrets/kafka/client.keystore.p12", properties.keystorePath)
        assertEquals("pam-import-api", properties.applicationName)
    }

    @Test
    fun `Kafka configuration permits local brokers without SSL credentials`() {
        val properties = NaisKafkaConfigProperties(mapOf("KAFKA_BROKERS" to "localhost:9092"))

        assertEquals("localhost:9092", properties.brokers)
        assertNull(properties.credstorePassword)
        assertNull(properties.truststorePath)
        assertNull(properties.keystorePath)
        assertNull(properties.applicationName)
    }

    @Test
    fun `Kafka configuration requires the Nais broker variable`() {
        val exception = assertThrows<IllegalStateException> {
            NaisKafkaConfigProperties(mapOf("KAFKA_BOOTSTRAP_SERVERS" to "localhost:9092"))
        }

        assertEquals("KAFKA_BROKERS er ikke angitt", exception.message)
    }
}
