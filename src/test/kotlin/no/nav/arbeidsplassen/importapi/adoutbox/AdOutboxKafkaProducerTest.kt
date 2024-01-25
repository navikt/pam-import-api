package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdOutboxKafkaProducerTest {
    @Test
    fun `AdOutboxKafkaProducer starter og klarer å produsere melding`() {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka")).use { container ->
            container.start()
            val config: Map<String, Any> = Collections.singletonMap(
                "kafka.brokers",
                container.getBootstrapServers()
            )

            ApplicationContext.run(config).use { ctx ->
                val producer: AdOutboxKafkaProducer = ctx.getBean(AdOutboxKafkaProducer::class.java)
                val recordMetadata = producer.sendAndGet(UUID.randomUUID().toString(), "hallo :)".toByteArray())
                recordMetadata.serializedValueSize()

                assertEquals("hallo :)".toByteArray().size, recordMetadata.serializedValueSize())
            }
        }
    }
}