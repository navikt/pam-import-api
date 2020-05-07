package no.nav.arbeidsplassen.importapi.exception

import io.micronaut.configuration.kafka.exceptions.DefaultKafkaListenerExceptionHandler
import io.micronaut.configuration.kafka.exceptions.KafkaListenerException
import io.micronaut.configuration.kafka.exceptions.KafkaListenerExceptionHandler
import io.micronaut.context.annotation.Replaces
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.slf4j.LoggerFactory
import java.util.regex.Pattern
import javax.inject.Singleton

@Singleton
@Replaces(bean = DefaultKafkaListenerExceptionHandler::class)
class KafkaExceptionHandler : KafkaListenerExceptionHandler {

    override fun handle(exception: KafkaListenerException) {
        val cause = exception.cause!!
        val consumerBean = exception.kafkaListener
        val kafkaConsumer = exception.kafkaConsumer

        if (cause is SerializationException) {
            LOG.error("Kafka consumer [" + consumerBean + "] failed to deserialize value: " + cause.message, cause)
            seekPastDeserializationError(cause, consumerBean, kafkaConsumer)
        } else {
            val consumerRecord = exception.consumerRecord
            if (consumerRecord.isPresent) {
                LOG.error("Error processing record [" + consumerRecord + "] for Kafka consumer [" + consumerBean + "] produced error: " + cause.message, cause)
            } else {
                LOG.error("Kafka consumer [" + consumerBean + "] produced error: " + cause.message, cause)
            }
            LOG.error("Pausing consumer, need further investigation.")
            kafkaConsumer.pause(kafkaConsumer.assignment())
        }
    }

    private fun seekPastDeserializationError( cause: SerializationException, consumerBean: Any, kafkaConsumer: Consumer<*, *>) {
        try {
            val message = cause.message
            val matcher = SERIALIZATION_EXCEPTION_MESSAGE_PATTERN.matcher(message)
            if (matcher.find()) {
                val topic = matcher.group(1)
                val partition = Integer.valueOf(matcher.group(2))
                val offset = Integer.valueOf(matcher.group(3))
                val tp = TopicPartition(topic, partition)
                LOG.warn("Seeking past undeserializable consumer record for partition {}-{} and offset {}", topic, partition, offset)
                kafkaConsumer.seek(tp, offset + 1.toLong())

            }
        } catch (e: Throwable) {
            LOG.error("Kafka consumer [" + consumerBean + "] failed to seek past undeserializable value: " + e.message, e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KafkaExceptionHandler::class.java)
        private val SERIALIZATION_EXCEPTION_MESSAGE_PATTERN = Pattern.compile(".+ for partition (.+)-(\\d+) at offset (\\d+)\\..+")
    }
}