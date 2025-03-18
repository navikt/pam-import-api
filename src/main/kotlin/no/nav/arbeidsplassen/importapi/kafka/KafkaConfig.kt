package no.nav.arbeidsplassen.importapi.kafka

import jakarta.inject.Singleton
import java.net.InetAddress
import java.util.Collections
import java.util.UUID
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

@Singleton
open class KafkaConfig(private val env: Map<String, String> = System.getenv()) {
    companion object {
        private val LOG = LoggerFactory.getLogger(KafkaConfig::class.java)
    }

    open fun kafkaJsonConsumer(topic: String, groupId: String): KafkaConsumer<String?, ByteArray?> {
        val consumer: KafkaConsumer<String?, ByteArray?> = KafkaConsumer(kafkaConsumerProperties(groupId))
        consumer.subscribeTilTopic(topic)
        return consumer
    }

    private fun KafkaConsumer<String?, *>.subscribeTilTopic(topic: String) {
        this.subscribe(Collections.singleton(topic), object : ConsumerRebalanceListener {
            override fun onPartitionsRevoked(partitions: Collection<TopicPartition?>) {
                partitions.forEach { tp ->
                    LOG.info("Rebalance: no longer assigned to topic ${tp?.topic()}, partition ${tp?.partition()}")
                }
            }

            override fun onPartitionsAssigned(partitions: Collection<TopicPartition?>) {
                partitions.forEach { tp ->
                    LOG.info("Rebalance: assigned to topic ${tp?.topic()}, partition ${tp?.partition()}")
                }
            }
        })
    }

    open fun kafkaProducer(): KafkaProducer<String, ByteArray?> {
        return KafkaProducer(kafkaProducerProperties())
    }

    private fun generateInstanceId(env: Map<String, String>): String {
        if (env.containsKey("NAIS_APP_NAME")) return env["NAIS_APP_NAME"] + ":" + InetAddress.getLocalHost().hostName
        return UUID.randomUUID().toString()
    }

    private fun kafkaConsumerProperties(groupId: String): Map<String, Any> {
        val clientId = generateInstanceId(env)
        val autoOffsetResetConfig = "earliest"

        val props = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = env["KAFKA_BROKERS"]!!

        if (!env["KAFKA_CREDSTORE_PASSWORD"].isNullOrBlank()) {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }

        env["KAFKA_TRUSTSTORE_PATH"]?.let { props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = it }
        env["KAFKA_KEYSTORE_PATH"]?.let { props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = it }
        env["KAFKA_TRUSTSTORE_PATH"]?.let { props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL" }

        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.CLIENT_ID_CONFIG] = clientId
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = autoOffsetResetConfig
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 500000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        props[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000

        return props
    }

    private fun kafkaProducerProperties(): Map<String, Any> {
        val clientId = generateInstanceId(env)

        val props = mutableMapOf<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = env["KAFKA_BROKERS"]!!

        if (!env["KAFKA_CREDSTORE_PASSWORD"].isNullOrBlank()) {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = env["KAFKA_CREDSTORE_PASSWORD"]!!
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }

        env["KAFKA_TRUSTSTORE_PATH"]?.let { props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = it }
        env["KAFKA_KEYSTORE_PATH"]?.let { props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = it }
        env["KAFKA_TRUSTSTORE_PATH"]?.let { props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL" }

        props[ProducerConfig.CLIENT_ID_CONFIG] = clientId
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.RETRIES_CONFIG] = Int.MAX_VALUE
        props[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 10100
        props[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 10000
        props[ProducerConfig.LINGER_MS_CONFIG] = 100
        props[ProducerConfig.BATCH_SIZE_CONFIG] = 16384 * 8
        props[ProducerConfig.ACKS_CONFIG] = "all"
        return props
    }
}
