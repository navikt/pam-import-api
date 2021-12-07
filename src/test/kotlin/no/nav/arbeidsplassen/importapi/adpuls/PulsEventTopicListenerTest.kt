package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(startApplication = false)
class PulsEventTopicListenerTest(private val pulsEventTopicListener: PulsEventTopicListener,
                                 private val adminStatusRepository: AdminStatusRepository,
                                 private val providerRepository: ProviderRepository) {

    @Test
    fun simulatePulsEventTopicListenerTest() {
        val provider = providerRepository.newTestProvider()
        val adminStatus = adminStatusRepository.save(AdminStatus(uuid = UUID.randomUUID().toString(), reference = UUID.randomUUID().toString(), providerId = provider.id!!, versionId = 1))
        val one = PulsEventDTO(type = PulsEventType.pageviews.value, oid = adminStatus.uuid, total = 5)
        val two = PulsEventDTO(type = PulsEventType.pageviews.value, oid = adminStatus.uuid, total = 10)
        val three = PulsEventDTO(type = PulsEventType.applicationurlclicks.value, oid = adminStatus.uuid, total = 15)
        val four = PulsEventDTO(type = PulsEventType.applicationurlclicks.value, oid = adminStatus.uuid, total = 25)
        val five = PulsEventDTO(type = "bogus", oid = adminStatus.uuid, total = 25)
        val dtos = pulsEventTopicListener.syncPulsEvents(listOf(one,two,three,four,five), listOf(1,2,3,4,5))
        assertEquals(2,dtos.size)
        assertEquals(10, dtos[0].total)
        assertEquals(25, dtos[1].total)
    }
}
