package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.context.annotation.Value
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeader

open class AdOutboxKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, ByteArray?>,
    @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}") private val topic: String,
    private val healthService: HealthService
) {
    private val headers = listOf(RecordHeader("@meldingstype", Meldingstype.IMPORT_API.name.toByteArray()))

    open fun sendAndGet(uuid: String, payload: ByteArray, meldingstype: Meldingstype): RecordMetadata =
        kafkaProducer.send(ProducerRecord(topic, null, uuid, payload, headers)).get()

    fun unhealthy() = healthService.addUnhealthyVote()

    enum class Meldingstype {
        IMPORT_API, ANNONSEMOTTAK
    }
}
