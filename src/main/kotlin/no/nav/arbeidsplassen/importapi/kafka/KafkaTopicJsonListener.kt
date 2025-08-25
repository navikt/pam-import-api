package no.nav.arbeidsplassen.importapi.kafka

import kotlin.concurrent.thread
import no.nav.arbeidsplassen.importapi.leaderelection.LeaderElection
import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.MDC

class KafkaTopicJsonListener(
    override val leaderElection: LeaderElection,
    override val kafkaConsumer: KafkaConsumer<String?, ByteArray?>,
    override val healthService: HealthService,
    private val messageListener: TopicMessageListener
) : KafkaTopicListener<ByteArray>() {
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

    interface TopicMessageListener {
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
