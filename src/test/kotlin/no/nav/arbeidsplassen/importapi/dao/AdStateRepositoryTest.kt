package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class AdStateRepositoryTest(private val adStateRepository: AdStateRepository,
                            private val objectMapper: ObjectMapper,
                            private val providerRepository: ProviderRepository,
                            private val transferLogRepository: TransferLogRepository) {

    @Test
    fun adStateCrudTest() {
        val provider = providerRepository.newTestProvider()
        val transfer = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), TransferDTO::class.java)
        val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring")
        val transferInDb = transferLogRepository.save(transferLog)
        val ad = transfer.ads[0]
        val adState = AdState(jsonPayload = objectMapper.writeValueAsString(ad), providerId = provider.id!!,
                reference = ad.reference, versionId = transferInDb.id!!)
        val created = adStateRepository.save(adState)
        assertNotNull(created.id)
        val read = adStateRepository.findById(created.id!!).get()
        adStateRepository.save(read.copy(reference = "123456"))
        val updated = adStateRepository.findById(created.id!!).get()
        assertEquals("123456", updated.reference)
        assertEquals(transferInDb.id!!, updated.versionId)
        adStateRepository.deleteById(created.id!!)
        val deleted = adStateRepository.findById(created.id!!)
        assertTrue(deleted.isEmpty)
        val adstate2 = AdState(jsonPayload = objectMapper.writeValueAsString(ad), providerId = provider.id!!,
                reference = "123456", versionId = transferInDb.id!!)
        val adstates = listOf(adState, adstate2)
        adStateRepository.saveAll(adstates)
        assertEquals(2, adStateRepository.findAll().count())
        val content = adStateRepository.list(provider.id!!, Pageable.from(0).order("updated", Sort.Order.Direction.DESC)).content
        assertNotNull(content)
        assertEquals(2,adStateRepository.findByUpdatedGreaterThanEquals(updated= LocalDateTime.now().minusDays(1), pageable = Pageable.from(0)).count())
    }
}