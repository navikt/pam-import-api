package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@MicronautTest
class AdStateRepositoryTest(
    private val adStateRepository: AdStateRepository,
    private val objectMapper: ObjectMapper,
    private val providerRepository: ProviderRepository,
    private val transferLogRepository: TransferLogRepository
) {

    @Test
    fun adStateCrudTest() {
        val provider = providerRepository.newTestProvider()
        val ads = objectMapper.transferToAdList()
        val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring", items = 1)
        val transferInDb = transferLogRepository.save(transferLog)
        val ad = ads[0]
        val adState = AdState(
            jsonPayload = objectMapper.writeValueAsString(ad), providerId = provider.id!!,
            reference = ad.reference, versionId = transferInDb.id!!
        )
        val created = adStateRepository.save(adState)
        assertNotNull(created.id)
        val read = adStateRepository.findById(created.id!!)!!
        adStateRepository.save(read.copy(reference = "123456"))
        val updated = adStateRepository.findById(created.id!!)!!
        assertEquals("123456", updated.reference)
        assertEquals(transferInDb.id!!, updated.versionId)
        adStateRepository.deleteById(created.id!!)
        val deleted = adStateRepository.findById(created.id!!)
        assertNull(deleted)
        val adstate2 = AdState(
            jsonPayload = objectMapper.writeValueAsString(ad), providerId = provider.id!!,
            reference = "123456", versionId = transferInDb.id!!
        )
        val adstates = listOf(adState, adstate2)
        adStateRepository.saveAll(adstates)
        assertEquals(2, adStateRepository.findAll().count())
    }
}
