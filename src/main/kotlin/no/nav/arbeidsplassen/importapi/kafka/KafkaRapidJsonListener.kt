package no.nav.arbeidsplassen.importapi.kafka

import kotlin.concurrent.thread
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.MDC

class KafkaRapidJsonListener(
    override val kafkaConsumer: KafkaConsumer<String?, ByteArray?>,
    val messageListener: RapidMessageListener,
    override val healthService: HealthService
) : KafkaRapidListener<ByteArray>() {
    override fun startListener(): Thread {
        return thread(name = "KafkaListener ${messageListener.javaClass}") { startListenerInternal() }
    }

    override fun handleRecord(record: ConsumerRecord<String?, ByteArray?>) = record.key()?.let { key ->
        try {
            MDC.put("U", key)
            val eventId = record.headers().headers("@eventId").firstOrNull()?.let { String(it.value()) }
            eventId?.let { MDC.put("TraceId", it) }
            val kilde = record.headers().headers("@kilde").firstOrNull()?.let { String(it.value()) }
            val message = JsonMessage(
                key, eventId, record.value()?.let { String(it) }, record.timestamp(),
                record.partition(), record.offset(), kilde
            )
            messageListener.onMessage(message)
        } finally {
            MDC.clear()
        }
    }

    interface RapidMessageListener {
        fun onMessage(message: JsonMessage)
    }

    class JsonMessage(
        val key: String,
        val eventId: String?,
        val payload: String?,
        val timestamp: Long? = null,
        val partition: Int? = null,
        val offset: Long? = null,
        val kilde: String? = null
    )
}
