package no.nav.arbeidsplassen.importapi.adoutbox

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import org.apache.kafka.common.KafkaException
import org.slf4j.LoggerFactory

class AdOutboxService(
    private val adOutboxKafkaProducer: AdOutboxKafkaProducer,
    private val adOutboxRepository: AdOutboxRepository,
    private val adStateService: AdStateService,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AdOutboxService::class.java)
    }

    fun lagreTilOutbox(adState: AdState) =
        adOutboxRepository.lagre(
            AdOutbox(
                uuid = adState.uuid,
                payload = objectMapper.writeValueAsString(adStateService.convertToInternalDto(adState))
            )
        )

    fun lagreFlereTilOutbox(adStates: Iterable<AdState>) = adStates
        .map {
            AdOutbox(
                uuid = it.uuid,
                payload = objectMapper.writeValueAsString(adStateService.convertToInternalDto(it))
            )
        }
        .let { adOutboxRepository.lagreFlere(it) }

    fun markerSomProsesert(adOutbox: AdOutbox): AdOutbox {
        val newValue = adOutbox.copy(harFeilet = false, prosessertDato = LocalDateTime.now())
        adOutboxRepository.markerSomProsessert(newValue)
        return newValue
    }

    fun markerSomFeilet(adOutbox: AdOutbox): AdOutbox {
        val newValue = adOutbox.copy(
            harFeilet = true,
            antallForsøk = adOutbox.antallForsøk + 1,
            sisteForsøkDato = LocalDateTime.now()
        )
        adOutboxRepository.markerSomFeilet(newValue)
        return newValue
    }

    fun hentUprosesserteMeldinger(batchSize: Int = 1000, outboxDelay: Long = 30): List<AdOutbox> =
        adOutboxRepository.hentUprosesserteMeldinger(batchSize, outboxDelay)

    fun prosesserAdOutboxMeldinger() {
        val uprossesserteMeldinger = hentUprosesserteMeldinger()
        var feilede = 0

        LOG.info("Publiserer ${uprossesserteMeldinger.size} adOutbox-meldinger")

        uprossesserteMeldinger.forEach { adOutbox ->
            try {
                adOutboxKafkaProducer.sendAndGet(adOutbox.uuid, adOutbox.payload.toByteArray(), Meldingstype.IMPORT_API)
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
