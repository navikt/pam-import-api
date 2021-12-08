package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*


@MicronautTest
class AdPulsTasksTest(private val adPulsTasks: AdPulsTasks, private val providerRepository: ProviderRepository,
                      private val adPulsService: AdPulsService) {

    @Test
    fun runAdPulsTaskTest() {
        val provider = providerRepository.newTestProvider()
        adPulsService.save(AdPulsDTO(uuid = UUID.randomUUID().toString(), providerId = provider.id!!, reference = UUID.randomUUID().toString(), total = 10, type = PulsEventType.pageviews))
        adPulsService.save(AdPulsDTO(uuid = UUID.randomUUID().toString(), providerId = provider.id!!, reference = UUID.randomUUID().toString(), total = 20, type = PulsEventType.pageviews))
        assertEquals(0, adPulsTasks.deleteOldAdPulsEvents(LocalDateTime.now().minusDays(3)))
        assertEquals(2,adPulsTasks.deleteOldAdPulsEvents(LocalDateTime.now().plusMonths(3)))
    }

}
