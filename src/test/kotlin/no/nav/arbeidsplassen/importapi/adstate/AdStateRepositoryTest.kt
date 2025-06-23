package no.nav.arbeidsplassen.importapi.adstate

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.app.test.TestRepositories
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Testcontainers
class AdStateRepositoryTest : TestRepositories() {

    private val adStateRepository: AdStateRepository = appCtx.databaseApplicationContext.adStateRepository
    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val transferLogRepository: TransferLogRepository = appCtx.databaseApplicationContext.transferLogRepository
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun adStateCrudTest() {
        txTemplate.doInTransactionNullable { ctx ->

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

            val adstate2Lastet = adStateRepository.findByUuidAndProviderId(adstate2.uuid, adstate2.providerId)
            assertNotNull(adstate2Lastet)
            assertEquals(adstate2.reference, adstate2Lastet!!.reference)

            ctx.setRollbackOnly()
        }
    }
}
