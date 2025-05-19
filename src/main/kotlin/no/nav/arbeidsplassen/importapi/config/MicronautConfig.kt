package no.nav.arbeidsplassen.importapi.config

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter

@Factory
class MicronautConfig {
    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter()
    }

    @Singleton
    @Requires(property = "adoutbox.kafka.enabled", value = "true")
    fun adOutboxProducer(
        @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}") topic: String,
        healthService: HealthService,
        kafkaConfig: KafkaConfig
    ): AdOutboxKafkaProducer =
        AdOutboxKafkaProducer(
            kafkaProducer = kafkaConfig.kafkaProducer(),
            topic = topic,
            healthService = healthService,
        )
}
