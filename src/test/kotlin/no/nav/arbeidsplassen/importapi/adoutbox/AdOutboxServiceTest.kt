package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class AdOutboxServiceTest(
    private val adOutboxService: AdOutboxService, private val adOutboxRepository: AdOutboxRepository
) {
    @Test
    fun `AdOutboxKafkaProducer starter og klarer å produsere melding`() {
        adOutboxService.lagreTilOutbox(AdState(providerId = 1, reference = "ref", versionId = 1, jsonPayload = """{"payload": true}"""))

        val initiellAdOutbox = adOutboxRepository.hentAlle().also { assertEquals(1, it.size) }.first()
        assertNull(initiellAdOutbox.prosessertDato)
        assertNull(initiellAdOutbox.sisteForsøkDato)
        assertFalse(initiellAdOutbox.harFeilet)

        val initiellUprosessert = adOutboxService.hentUprosesserteMeldinger(outboxDelay = 0).also { assertEquals(1, it.size) }.first()
        assertEquals(initiellAdOutbox, initiellUprosessert)

        adOutboxService.markerSomFeilet(initiellAdOutbox)

        val feiletAdOutbox = adOutboxRepository.hentAlle().also { assertEquals(1, it.size) }.first()
        assertNull(feiletAdOutbox.prosessertDato)
        assertNotNull(feiletAdOutbox.sisteForsøkDato)
        assertTrue(feiletAdOutbox.harFeilet)
        assertEquals(initiellAdOutbox.uuid, feiletAdOutbox.uuid)
        assertEquals(initiellAdOutbox.id, feiletAdOutbox.id)
        assertEquals(initiellAdOutbox.payload, feiletAdOutbox.payload)

        adOutboxService.markerSomProsesert(feiletAdOutbox)
        val prosessertAdOubox = adOutboxRepository.hentAlle().also { assertEquals(1, it.size) }.first()
        assertNotNull(prosessertAdOubox.prosessertDato)
        assertNotNull(prosessertAdOubox.sisteForsøkDato)
        assertTrue(prosessertAdOubox.harFeilet)
        assertEquals(initiellAdOutbox.uuid, prosessertAdOubox.uuid)
        assertEquals(initiellAdOutbox.id, prosessertAdOubox.id)
        assertEquals(initiellAdOutbox.payload, prosessertAdOubox.payload)

        assertEquals(0, adOutboxRepository.hentUprosesserteMeldinger(outboxDelay = 0).size)
    }

    private fun AdOutboxRepository.hentAlle() = connection
        .prepareStatement("SELECT * FROM ad_outbox").executeQuery()
        .use { generateSequence { if (it.next()) it.toAdOutbox() else null }.toList() }
}
