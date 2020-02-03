package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.transfer.Transfer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class AdStateRepositoryTest(private val adStateRepository: AdStateRepository,
                            private val objectMapper: ObjectMapper,
                            private val  providerRepository: ProviderRepository) {

    @Test
    fun adStateCrudTest() {
        val provider = Provider(email = "test@test.test", username = "tester")
        val providerinDB = providerRepository.save(provider)
        val transfer = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), Transfer::class.java)
        val ad = transfer.ads[0]
        val adState = AdState(jsonPayload = objectMapper.writeValueAsString(ad),providerId = providerinDB.id!!, reference = ad.reference)
        val created = adStateRepository.save(adState)
        assertNotNull(created.id)
        val read = adStateRepository.findById(created.id!!).get()
        adStateRepository.save(read.copy(reference = "123456"))
        val updated = adStateRepository.findById(created.id!!).get()
        assertEquals("123456", updated.reference)
        println(updated)
        adStateRepository.deleteById(created.id!!)
        val deleted = adStateRepository.findById(created.id!!)
        assertTrue(deleted.isEmpty)
        val adstate2 = AdState(jsonPayload = objectMapper.writeValueAsString(ad),providerId = providerinDB.id!!, reference = "123456")
        val adstates = mutableListOf(adState, adstate2)
        adStateRepository.saveAll(adstates)
        assertEquals(2, adStateRepository.findAll().count())
    }
}