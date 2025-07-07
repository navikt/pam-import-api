package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdStateDTOTest : TestRunningApplication() {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AdStateDTOTest::class.java)
    }

    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val transferLogRepository: TransferLogRepository = appCtx.databaseApplicationContext.transferLogRepository
    private val adstateRepository: AdStateRepository = appCtx.databaseApplicationContext.adStateRepository
    private val adStateService: AdStateService = appCtx.servicesApplicationContext.adStateService
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun testAdStateAndGenerateDTOJson() {
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
            val save = adstateRepository.save(adState)
            Assertions.assertNotNull(save.id)
            LOG.info(objectMapper.writeValueAsString(adStateService.getAdStateByUuid(save.uuid)))

            ctx.setRollbackOnly()
        }
    }


}
