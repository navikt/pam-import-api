package no.nav.arbeidsplassen.importapi.adoutbox

import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer.Meldingstype.IMPORT_API
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdOutboxKafkaProducerTest {
    @Test
    fun `AdOutboxKafkaProducer starter og klarer å produsere melding`() {

        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0")).use { container ->
            container.start()

            val healthService = HealthService()
            val producer = AdOutboxKafkaProducer(
                kafkaProducer = KafkaConfig(mapOf("KAFKA_BROKERS" to container.bootstrapServers)).kafkaProducer(),
                topic = "teampam.annonsemottak-1",
                healthService = healthService
            )

            val recordMetadata = producer.sendAndGet(
                UUID.randomUUID().toString(),
                "hallo :)".toByteArray(),
                IMPORT_API
            )
            assertEquals("hallo :)".toByteArray().size, recordMetadata.serializedValueSize())
            assertTrue(healthService.isHealthy())
        }
    }
}
