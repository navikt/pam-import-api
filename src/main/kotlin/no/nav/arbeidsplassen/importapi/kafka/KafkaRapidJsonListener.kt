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
            MDC.put("AD", key)
            record.headers().headers("kafka_correlationId").firstOrNull()
                ?.let { MDC.put("C", String(it.value())) }
            
            val message = JsonMessage(
                key, record.value()?.let { String(it) }, record.timestamp(),
                record.partition(), record.offset()
            )
            messageListener.onMessage(message)
        } finally {
            MDC.clear()
        }
    }

    interface RapidMessageListener {
        fun onMessage(message: JsonMessage)
    }

    data class JsonMessage(
        val key: String,
        val payload: String?,
        val timestamp: Long? = null,
        val partition: Int? = null,
        val offset: Long? = null,
    )
}
