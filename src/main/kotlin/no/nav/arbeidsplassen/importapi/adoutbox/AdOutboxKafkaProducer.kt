package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.exception.KafkaStateRegistry
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata


@Singleton
class AdOutboxKafkaProducer(
    @KafkaClient("ad-outbox-producer") private val adOutboxProducer: KafkaProducer<String, String?>,
    @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}") private val topic: String,
    private val kafkaStateRegistry: KafkaStateRegistry
) {
    fun sendAndGet(uuid: String, adOutbox: String): RecordMetadata =
        adOutboxProducer.send(ProducerRecord(topic, uuid, adOutbox)).get()

    fun unhealthy() = kafkaStateRegistry.setProducerToError("ad-outbox-producer")
}
