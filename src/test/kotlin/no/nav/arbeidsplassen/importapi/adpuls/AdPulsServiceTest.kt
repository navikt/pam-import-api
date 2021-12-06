package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AdPulsServiceTest(private val adPulsService: AdPulsService, private val  providerRepository: ProviderRepository) {

    @Test
    fun testAdPulsReadAndSaveService() {
        val provider = providerRepository.newTestProvider()
        val reference = UUID.randomUUID().toString()
        val uuid = UUID.randomUUID().toString()
        val adPulsDTO = adPulsService.save(AdPulsDTO(providerId = provider.id!!, total = 10, reference = reference, type = PulsEventType.pageviews, uuid = uuid))
        assertNotNull(adPulsDTO.id)
        val inDb = adPulsService.findByUuidAndType(uuid, PulsEventType.pageviews)
        assertNotNull(inDb!!.id)
        assertEquals(adPulsDTO.id, inDb.id)
        val empty = adPulsService.saveAll(listOf())
        assertEquals(0,empty.size)
        val savedList =  adPulsService.saveAll(listOf(adPulsDTO, inDb))
        assertEquals(2, savedList.size)
    }

}
