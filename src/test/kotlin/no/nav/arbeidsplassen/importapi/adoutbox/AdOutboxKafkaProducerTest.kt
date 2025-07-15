package no.nav.arbeidsplassen.importapi.adoutbox

import java.util.UUID
import no.nav.arbeidsplassen.importapi.config.TestKafkaConfigProperties
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.kafka.ConfluentKafkaContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdOutboxKafkaProducerTest {
    @Test
    fun `AdOutboxKafkaProducer starter og klarer å produsere melding`() {

        ConfluentKafkaContainer("confluentinc/cp-kafka:7.7.0").use { container ->
            container.start()

            val healthService = HealthService()
            val producer = AdOutboxKafkaProducer(
                synchronousKafkaSendAndGet = KafkaAdOutboxSendAndGet(
                    kafkaProducer =
                        KafkaConfig(TestKafkaConfigProperties(mapOf("KAFKA_BROKERS" to container.bootstrapServers))).kafkaProducer()
                ),
                topic = "teampam.annonsemottak-1",
                healthService = healthService
            )

            val recordMetadata = producer.sendAndGet(
                UUID.randomUUID().toString(),
                "hallo :)".toByteArray(),
                Meldingstype.IMPORT_API
            )
            assertEquals("hallo :)".toByteArray().size, recordMetadata.serializedValueSize())
            assertTrue(healthService.isHealthy())

            container.stop()
        }
    }
}
