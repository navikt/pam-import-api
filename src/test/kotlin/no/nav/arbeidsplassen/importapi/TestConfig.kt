package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import no.nav.arbeidsplassen.importapi.ontologi.EscoDTO
import no.nav.arbeidsplassen.importapi.ontologi.KonseptGrupperingDTO
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.mockito.Mockito
import org.mockito.kotlin.mock

@Factory
class TestConfig {

    @Replaces(LokalOntologiGateway::class)
    @Singleton
    class MockLokalOntologiGateway : LokalOntologiGateway("URL") {
        @Override
        override fun hentTypeaheadStilling(stillingstittel: String): List<Typeahead> {
            return listOf()
        }

        @Override
        override fun hentStyrkOgEscoKonsepterBasertPaJanzz(konseptId: Long): KonseptGrupperingDTO? {
            return KonseptGrupperingDTO(
                konseptId,
                "janzzForKonseptId=$konseptId", listOf("2221", "2223"), EscoDTO(
                    "escolabelForKonseptId=$konseptId",
                    "escouriForKonseptId=$konseptId"
                )
            )
        }
    }

    @Replaces(AdOutboxKafkaProducer::class)
    @Singleton
    @Requires(property = "adoutbox.kafka.enabled", value = "false")
    class AdOutboxKafkaProducerMock(healthService: HealthService) :
        AdOutboxKafkaProducer(mock { Mockito.mock(KafkaProducer::class.java) }, "test", healthService) {
        override fun sendAndGet(uuid: String, payload: ByteArray, meldingstype: Meldingstype): RecordMetadata =
            RecordMetadata(TopicPartition("test", 0), 0, 0, 0, 0, 0, 0)
    }
}
