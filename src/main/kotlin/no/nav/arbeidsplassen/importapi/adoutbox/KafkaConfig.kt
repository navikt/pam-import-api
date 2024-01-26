package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.net.InetAddress
import java.util.*

@Singleton
@Requires(property = "adoutbox.kafka.enabled", value="true")
open class KafkaConfig(
    @Value("\${kafka.brokers}") private val brokers: String,
    @Value("\${kafka.credstorepass}") private val credstorePassword: String?,
    @Value("\${kafka.truststorepath}") private val truststorePath: String?,
    @Value("\${kafka.keystorepath}") private val keystorePath: String?,
    @Value("\${appname}") private val appName: String?,
){
    open fun kafkaProducer() : KafkaProducer<String, ByteArray?> {
        return KafkaProducer(kafkaProducerProperties())
    }

    private fun generateInstanceId(): String =
        appName?.let { "$it:${InetAddress.getLocalHost().hostName}" } ?: UUID.randomUUID().toString()


    private fun kafkaProducerProperties(): Map<String, Any> {
        val clientId = generateInstanceId()

        val props = mutableMapOf<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokers

        if (!credstorePassword.isNullOrBlank()) {
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        }

        if (!truststorePath.isNullOrBlank()) {
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
        }

        if (!keystorePath.isNullOrBlank()) {
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath
        }

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
