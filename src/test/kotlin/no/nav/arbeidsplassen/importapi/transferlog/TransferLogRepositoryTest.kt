package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.AdStateRepositoryTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferJsonString
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.PamImportPageable
import no.nav.arbeidsplassen.importapi.toMD5Hex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@MicronautTest
class TransferLogRepositoryTest(
    private val providerRepository: ProviderRepository,
    private val transferLogRepository: TransferLogRepository,
    private val objectMapper: ObjectMapper
) {
    @Test
    fun transferLogCrudTest() {
        val provider = providerRepository.newTestProvider()
        val payload = objectMapper.transferJsonString()
        val md5hash = payload.toMD5Hex()
        println("md5hash: $md5hash")
        val transferLog = TransferLog(providerId = provider.id!!, md5 = md5hash, payload = payload, items = 1)
        val create = transferLogRepository.save(transferLog)
        val read = transferLogRepository.findById(create.id!!)!!
        assertNotNull(read)
        assertTrue(transferLogRepository.existsByProviderIdAndMd5(provider.id!!, md5hash))
        val updated = transferLogRepository.save(read.copy(status = TransferLogStatus.DONE))
        println(updated)
        transferLogRepository.deleteById(updated.id!!)
        assertEquals(0, transferLogRepository.findAll().count())

        val ad = objectMapper.transferToAdList()[0]
        val ads = mutableListOf<AdDTO>()
        for (i in 1..100) {
            ads.add(ad.copy(reference = i.toString()))
        }
        val payload2 = objectMapper.writeValueAsString(ads)
        transferLogRepository.save(
            TransferLog(
                providerId = provider.id!!,
                md5 = "md5hash",
                payload = payload2,
                items = 100
            )
        )
        val findByStatus = transferLogRepository.findByStatus(
            TransferLogStatus.RECEIVED,
            PamImportPageable(size = 1000, number = 0)
        )
        assertNotNull(findByStatus)
    }

    @Test
    fun transferlogMD5Test() {
        val transferJsonNode1 = objectMapper.readValue(
            AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"),
            JsonNode::class.java
        )
        val payload = objectMapper.writeValueAsString(transferJsonNode1)
        val md5hash = payload.toMD5Hex()
        val transferJsonNode2 = objectMapper.readValue(
            AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"),
            JsonNode::class.java
        )
        val payload2 = objectMapper.writeValueAsString(transferJsonNode2)
        val md5hash2 = payload2.toMD5Hex()
        assertEquals(md5hash, md5hash2)
    }
}
