package no.nav.arbeidsplassen.importapi.adoutbox

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer.AnnonsemottakKafkaPayload
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer.Meldingstype.IMPORT_API
import no.nav.arbeidsplassen.importapi.adstate.AdState
import org.apache.kafka.common.KafkaException
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AdOutboxService(
    private val adOutboxKafkaProducer: AdOutboxKafkaProducer,
    private val adOutboxRepository: AdOutboxRepository,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AdOutboxService::class.java)
    }

    fun lagreTilOutbox(adState: AdState) =
        adOutboxRepository.lagre(AdOutbox(uuid = adState.uuid, payload = objectMapper.writeValueAsString(adState)))

    fun lagreFlereTilOutbox(adStates: Iterable<AdState>) = adStates
        .map { AdOutbox(uuid = it.uuid, payload = objectMapper.writeValueAsString(it)) }
        .let { adOutboxRepository.lagreFlere(it) }

    fun markerSomProsesert(adOutbox: AdOutbox): AdOutbox {
        val newValue = adOutbox.copy(harFeilet = false, prosessertDato = LocalDateTime.now())
        adOutboxRepository.markerSomProsessert(newValue)
        return newValue
    }

    fun markerSomFeilet(adOutbox: AdOutbox): AdOutbox {
        val newValue = if (!adOutbox.harFeilet) adOutbox.copy(harFeilet = true)
        else adOutbox.copy(antallForsøk = adOutbox.antallForsøk + 1, sisteForsøkDato = LocalDateTime.now())

        adOutboxRepository.markerSomFeilet(newValue)
        return newValue
    }

    fun hentUprosesserteMeldinger(): List<AdOutbox> = adOutboxRepository.hentUprosesserteMeldinger()

    fun prosesserAdOutboxMeldinger() {
        val uprossesserteMeldinger = hentUprosesserteMeldinger()
        var feilede = 0

        LOG.info("Publiserer ${uprossesserteMeldinger.size} adOutbox-meldinger")

        uprossesserteMeldinger.forEach { adOutbox ->
            try {
                val melding = AnnonsemottakKafkaPayload(adOutbox.uuid, adOutbox.payload, IMPORT_API)
                adOutboxKafkaProducer.sendAndGet(adOutbox.uuid, objectMapper.writeValueAsBytes(melding))
                markerSomProsesert(adOutbox)
            } catch (e: KafkaException) {
                LOG.warn("Feil ved prosessering av adOutbox med id ${adOutbox.id} - Stilling ${adOutbox.uuid} - $e")
                if (markerSomFeilet(adOutbox).antallForsøk > 5) adOutboxKafkaProducer.unhealthy()
                feilede++
            } catch (e: Exception) {
                LOG.error("Uventet feil ved prosessering av adOutbox med id ${adOutbox.id} - Stilling ${adOutbox.uuid} - $e")
                if (markerSomFeilet(adOutbox).antallForsøk > 5) adOutboxKafkaProducer.unhealthy()
                feilede++
            }
        }

        LOG.info("Ferdig med å publiseere ${uprossesserteMeldinger.size} - Suksess: ${uprossesserteMeldinger.size - feilede} - Feilet: $feilede")
    }
}
