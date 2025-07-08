package no.nav.arbeidsplassen.importapi.adoutbox

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdOutboxServiceTest : TestRunningApplication() {

    private val adOutboxService: AdOutboxService = appCtx.servicesApplicationContext.adOutboxService
    private val adOutboxRepository: AdOutboxRepository = appCtx.databaseApplicationContext.adOutboxRepository
    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val providerService: ProviderService = appCtx.securityServicesApplicationContext.providerService
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun `AdOutboxService prosesserer og markerer outboxelementer riktig`() {
        txTemplate.doInTransactionNullable { ctx ->
            val provider = providerService.save(ProviderDTO(identifier = "test", email = "test", phone = "test"))

            val ad = AdDTO(
                published = LocalDateTime.now(), expires = LocalDateTime.now(),
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title",
                locationList = listOf(LocationDTO(country = "Danmark"))
            )

            adOutboxService.lagreFlereTilOutbox(
                listOf(
                    AdState(
                        providerId = provider.id!!,
                        reference = "ref",
                        versionId = 1,
                        jsonPayload = objectMapper.writeValueAsString(ad)
                    )
                )
            )

            val initiellAdOutbox = adOutboxRepository.hentAlle().also { assertEquals(1, it.size) }.first()
            assertNull(initiellAdOutbox.prosessertDato)
            assertNull(initiellAdOutbox.sisteForsøkDato)
            assertFalse(initiellAdOutbox.harFeilet)

            val initiellUprosessert =
                adOutboxService.hentUprosesserteMeldinger(outboxDelay = 0).also { assertEquals(1, it.size) }.first()
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

            ctx.setRollbackOnly()
        }
    }
}
