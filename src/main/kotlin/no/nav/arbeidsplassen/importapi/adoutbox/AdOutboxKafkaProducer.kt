package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.exception.KafkaStateRegistry
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeader

@Singleton
open class AdOutboxKafkaProducer(
    @KafkaClient("ad-outbox-producer") private val adOutboxProducer: KafkaProducer<String, ByteArray?>,
    @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}") private val topic: String,
    private val kafkaStateRegistry: KafkaStateRegistry
) {
    private val headers = listOf(RecordHeader("@meldingstype", Meldingstype.IMPORT_API.name.toByteArray()))

    open fun sendAndGet(uuid: String, payload: ByteArray, meldingstype: Meldingstype): RecordMetadata =
        adOutboxProducer.send(ProducerRecord(topic, null, uuid, payload, headers)).get()

    fun unhealthy() = kafkaStateRegistry.setProducerToError("ad-outbox-producer")

    enum class Meldingstype {
        IMPORT_API, ANNONSEMOTTAK
    }
}
