package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepositoryTest
import no.nav.arbeidsplassen.importapi.app.TestRepositories
import no.nav.arbeidsplassen.importapi.common.toMD5Hex
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferJsonString
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.provider.Provider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogRepositoryTest : TestRepositories() {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(TransferLogRepositoryTest::class.java)
    }

    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val transferLogRepository: TransferLogRepository = appCtx.databaseApplicationContext.transferLogRepository
    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun transferLogCrudTest() {
        txTemplate.doInTransactionNullable { ctx ->

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
            val findByStatus = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED)
            assertNotNull(findByStatus)

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun transferLogOrderTest() {
        txTemplate.doInTransactionNullable { ctx ->

            val provider =
                providerRepository.save(
                    Provider(
                        identifier = "tester2",
                        email = "tester2@tester.test",
                        phone = "12345678"
                    )
                )
            val payload = objectMapper.transferJsonString()
            val md5hash = payload.toMD5Hex()
            val transferLog1 = TransferLog(providerId = provider.id!!, md5 = "1", payload = payload, items = 1)
            val transferLog2 = TransferLog(providerId = provider.id!!, md5 = "2", payload = payload, items = 1)
            val create1 = transferLogRepository.save(transferLog1)
            val read1 = transferLogRepository.findById(create1.id!!)!!
            val create2 = transferLogRepository.save(transferLog2)
            val read2 = transferLogRepository.findById(create2.id!!)!!
            assertNotNull(read1)
            assertNotNull(read2)

            val findByStatus = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED)
            assertNotNull(findByStatus)
            assertEquals(2, findByStatus.size)
            LOG.info("Updated: ${findByStatus[0].updated} ${findByStatus[1].updated}")
            assertTrue(!findByStatus[0].updated.isAfter(findByStatus[1].updated))

            //Rydde opp:
            findByStatus.map { it.id!! }.forEach {
                transferLogRepository.deleteById(it)
            }

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun transferlogMD5Test() {
        txTemplate.doInTransactionNullable { ctx ->

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

            ctx.setRollbackOnly()
        }
    }
}
