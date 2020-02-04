package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.md5Hex
import no.nav.arbeidsplassen.importapi.transfer.Ad
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
        val transferJsonNode = objectMapper.readValue(AdStateRepositoryTest::class.java.getResourceAsStream("/transfer-ads.json"), JsonNode::class.java)
        val payload = objectMapper.writeValueAsString(transferJsonNode)
        val md5hash = payload.md5Hex()
        println ("md5hash: $md5hash")
        val transferLog = TransferLog(providerId = providerinDB.id!!,md5 = md5hash, payload = payload)
        val create = transferLogRepository.save(transferLog)
        val read = transferLogRepository.findById(create.id!!).get()
        assertNotNull(read)
        assertTrue(transferLogRepository.existsByProviderIdAndMd5( providerinDB.id!!,md5hash))
        val updated = transferLogRepository.save(read.copy(status=TransferLogStatus.DONE))
        println(updated)
        transferLogRepository.deleteById(updated.id!!)
        assertEquals(0,transferLogRepository.findAll().count())
        val transfer = objectMapper.treeToValue(transferJsonNode, Transfer::class.java)
        val ad = transfer.ads[0]
        var ads = mutableListOf<Ad>()
        for (i in 1..100 ) {
            ads.add(ad.copy(reference=i.toString()))
        }
        val newTransfer = transfer.copy(ads = ads)
        val start = System.currentTimeMillis()
        val payload2 = objectMapper.writeValueAsString(newTransfer)
        println("length: ${payload2.toByteArray().size}")
        transferLogRepository.save(TransferLog(providerId = providerinDB.id!!, md5="md5hash", payload = payload2))
        println("time ${System.currentTimeMillis()-start}")
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
