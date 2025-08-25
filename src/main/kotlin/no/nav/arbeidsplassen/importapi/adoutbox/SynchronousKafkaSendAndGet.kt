package no.nav.arbeidsplassen.importapi.adoutbox

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.internals.RecordHeader

interface SynchronousKafkaSendAndGet {
    fun sendAndGet(topic: String, uuid: String, payload: ByteArray, meldingstype: Meldingstype): RecordMetadata
}

class KafkaAdOutboxSendAndGet(
    private val kafkaProducer: Producer<String, ByteArray?>,
) : SynchronousKafkaSendAndGet {

    private val headers = listOf(RecordHeader("@meldingstype", Meldingstype.IMPORT_API.name.toByteArray()))

    override fun sendAndGet(
        topic: String,
        uuid: String,
        payload: ByteArray,
        meldingstype: Meldingstype
    ): RecordMetadata {
        return kafkaProducer.send(ProducerRecord(topic, null, uuid, payload, headers)).get()
    }

}
