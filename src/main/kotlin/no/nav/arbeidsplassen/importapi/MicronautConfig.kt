package no.nav.arbeidsplassen.importapi

import io.micronaut.configuration.kafka.KafkaProducerFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.adoutbox.KafkaConfig
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import org.apache.kafka.clients.producer.KafkaProducer

@Factory
class MicronautConfig {
    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter()
    }

    @Singleton
    @Requires(property = "adoutbox.kafka.enabled", value="true")
    @Replaces(factory = KafkaProducerFactory::class)
    @Primary
    fun adOutboxProducer(kafkaConfig: KafkaConfig): KafkaProducer<String, ByteArray?> =
        kafkaConfig.kafkaProducer()
}
