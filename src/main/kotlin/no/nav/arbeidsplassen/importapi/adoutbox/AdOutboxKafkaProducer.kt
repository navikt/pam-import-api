package no.nav.arbeidsplassen.importapi.adoutbox

import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeader

class AdOutboxKafkaProducer(
    private val kafkaProducer: KafkaProducer<String, ByteArray?>,
    private val topic: String, // TODO @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}")
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
