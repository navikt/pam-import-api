package no.nav.arbeidsplassen.importapi.adpuls

import java.time.LocalDateTime
import java.util.UUID
import no.nav.arbeidsplassen.importapi.app.test.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdPulsTasksTest : TestRunningApplication() {

    private val adPulsTasks: AdPulsTasks = appCtx.servicesApplicationContext.adPulsTasks
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val adPulsService: AdPulsService = appCtx.servicesApplicationContext.adPulsService

    @Test
    fun runAdPulsTaskTest() {
        val provider = providerRepository.newTestProvider()
        adPulsService.save(
            AdPulsDTO(
                uuid = UUID.randomUUID().toString(),
                providerId = provider.id!!,
                reference = UUID.randomUUID().toString(),
                total = 10,
                type = PulsEventType.pageviews
            )
        )
        adPulsService.save(
            AdPulsDTO(
                uuid = UUID.randomUUID().toString(),
                providerId = provider.id!!,
                reference = UUID.randomUUID().toString(),
                total = 20,
                type = PulsEventType.pageviews
            )
        )
        assertEquals(0, adPulsTasks.deleteOldAdPulsEvents(LocalDateTime.now().minusDays(3)))
        assertEquals(2, adPulsTasks.deleteOldAdPulsEvents(LocalDateTime.now().plusMonths(3)))
    }

}
