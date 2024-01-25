package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.adoutbox.KafkaConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.net.InetAddress
import java.util.*

@Factory
class MicronautConfig {
    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter()
    }

    @Singleton
    fun adOutboxProducer(kafkaConfig: KafkaConfig): KafkaProducer<String, ByteArray?> = kafkaConfig.kafkaProducer()
}
