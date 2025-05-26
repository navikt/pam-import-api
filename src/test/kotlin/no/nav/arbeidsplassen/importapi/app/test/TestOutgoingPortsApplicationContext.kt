package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.adoutbox.Meldingstype
import no.nav.arbeidsplassen.importapi.adoutbox.SynchronousKafkaSendAndGet
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.leaderelection.LeaderElection
import no.nav.arbeidsplassen.importapi.ontologi.EscoDTO
import no.nav.arbeidsplassen.importapi.ontologi.KonseptGrupperingDTO
import no.nav.arbeidsplassen.importapi.ontologi.OntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

class TestOutgoingPortsApplicationContext : OutgoingPortsApplicationContext {
    override val synchronousKafkaSendAndGet = object : SynchronousKafkaSendAndGet {
        override fun sendAndGet(
            topic: String,
            uuid: String,
            payload: ByteArray,
            meldingstype: Meldingstype
        ): RecordMetadata = RecordMetadata(TopicPartition("test", 0), 0, 0, 0, 0, 0, 0)
    }

    override val leaderElection = object : LeaderElection {
        override fun isLeader(): Boolean {
            return false
        }
    }

    override val ontologiGateway = object : OntologiGateway {
        override fun hentTypeaheadStillingerFraOntologi(): List<Typeahead> {
            return listOf()
        }

        override fun hentTypeaheadStilling(stillingstittel: String): List<Typeahead> {
            return listOf()
        }

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
}
