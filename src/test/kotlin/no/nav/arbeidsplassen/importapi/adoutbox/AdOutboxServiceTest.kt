package no.nav.arbeidsplassen.importapi.adoutbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class AdOutboxServiceTest(
    private val adOutboxService: AdOutboxService,
    private val adOutboxRepository: AdOutboxRepository,
    private val objectMapper: ObjectMapper,
    private val providerService: ProviderService
) {
    @Test
    fun `AdOutboxService prosesserer og markerer outboxelementer riktig`() {
        val provider = providerService.save(ProviderDTO(identifier = "test", email = "test", phone = "test"))

        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(country = "Danmark"))
        )

        adOutboxService.lagreTilOutbox(AdState(providerId = provider.id!!, reference = "ref", versionId = 1, jsonPayload = objectMapper.writeValueAsString(ad)))

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
