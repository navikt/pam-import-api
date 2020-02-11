package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.AdStateRepositoryTest
import no.nav.arbeidsplassen.importapi.dao.ProviderRepository
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.md5Hex
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class TransferLogRepositoryTest(private val providerRepository: ProviderRepository,
                                private val transferLogRepository: TransferLogRepository,
                                private val objectMapper: ObjectMapper) {
    @Test
    fun transferLogCrudTest() {
        val provider = providerRepository.newTestProvider()
        val transferJsonNode = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), JsonNode::class.java)
        val payload = objectMapper.writeValueAsString(transferJsonNode)
        val md5hash = payload.md5Hex()
        println ("md5hash: $md5hash")
        val transferLog = TransferLog(providerId = provider.id!!, md5 = md5hash, payload = payload)
        val create = transferLogRepository.save(transferLog)
        val read = transferLogRepository.findById(create.id!!).get()
        assertNotNull(read)
        assertTrue(transferLogRepository.existsByProviderIdAndMd5( provider.id!!,md5hash))
        val updated = transferLogRepository.save(read.copy(status= TransferLogStatus.DONE))
        println(updated)
        transferLogRepository.deleteById(updated.id!!)
        assertEquals(0,transferLogRepository.findAll().count())
        val transfer = objectMapper.treeToValue(transferJsonNode, TransferDTO::class.java)
        val ad = transfer.ads[0]
        var ads = mutableListOf<AdDTO>()
        for (i in 1..100 ) {
            ads.add(ad.copy(reference=i.toString()))
        }
        val newTransfer = transfer.copy(ads = ads)
        val payload2 = objectMapper.writeValueAsString(newTransfer)
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = "md5hash", payload = payload2))
        val findByStatus = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED,Pageable.from(0,100, Sort.of(Sort.Order.asc("updated"))))
        println(findByStatus.size)
    }

    @Test
    fun transferlogMD5Test() {
        val transferJsonNode1 = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), JsonNode::class.java)
        val payload = objectMapper.writeValueAsString(transferJsonNode1)
        val md5hash = payload.md5Hex()
        val transferJsonNode2 =  objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), JsonNode::class.java)
        val payload2 = objectMapper.writeValueAsString(transferJsonNode2)
        val md5hash2 = payload2.md5Hex()
        assertEquals(md5hash, md5hash2)
    }
}
