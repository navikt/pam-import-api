package no.nav.arbeidsplassen.importapi.adoutbox

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.net.InetAddress
import java.util.*

open class KafkaConfig {
    private val env = System.getenv()

    open fun kafkaProducer() : KafkaProducer<String, String?> {
        return KafkaProducer(kafkaProducerProperties())
    }

    private fun generateInstanceId(): String {
        if (env.containsKey("NAIS_APP_NAME")) return env["NAIS_APP_NAME"] + ":" + InetAddress.getLocalHost().hostName
        return UUID.randomUUID().toString()
    }

    private fun kafkaProducerProperties(): Map<String, Any> {
        val clientId = generateInstanceId()

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
        props[ProducerConfig.BATCH_SIZE_CONFIG] = 16384*8
        props[ProducerConfig.ACKS_CONFIG] = "all"
        return props
    }
}
