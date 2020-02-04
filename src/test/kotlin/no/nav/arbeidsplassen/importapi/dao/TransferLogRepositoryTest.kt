package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.transfer.Transfer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class TransferLogRepositoryTest(private val providerRepository: ProviderRepository,
                                private val transferLogRepository: TransferLogRepository,
                                private val objectMapper: ObjectMapper) {

    @Test
    fun transferLogCrudTest() {
        val provider = Provider(email = "transfer@test.test", username = "transfer")
        val providerinDB = providerRepository.save(provider)
        val transfer = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), Transfer::class.java)
        val transferLog = TransferLog(providerId = providerinDB.id!!,md5 = "1a2b3c4d5e", payload = objectMapper.writeValueAsString(transfer))
        val create = transferLogRepository.save(transferLog)
        val read = transferLogRepository.findById(create.id!!).get()
        assertNotNull(read)
        assertTrue(transferLogRepository.existsByProviderIdAndMd5( providerinDB.id!!,"1a2b3c4d5e"))
        val updated = transferLogRepository.save(read.copy(status=TransferLogStatus.DONE))
        println(updated)
        transferLogRepository.deleteById(updated.id!!)
        assertEquals(0,transferLogRepository.findAll().count())
    }
}
