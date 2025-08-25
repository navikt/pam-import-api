package no.nav.arbeidsplassen.importapi.adpuls

import java.util.UUID
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdPulsServiceTest : TestRunningApplication() {

    private val adPulsService: AdPulsService = appCtx.servicesApplicationContext.adPulsService
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate


    @Test
    fun testAdPulsReadAndSaveService() {
        txTemplate.doInTransactionNullable { ctx ->

            val provider = providerRepository.newTestProvider()
            val reference = UUID.randomUUID().toString()
            val uuid = UUID.randomUUID().toString()
            val adPulsDTO = adPulsService.save(
                AdPulsDTO(
                    providerId = provider.id!!,
                    total = 10,
                    reference = reference,
                    type = PulsEventType.pageviews,
                    uuid = uuid
                )
            )
            assertNotNull(adPulsDTO.id)
            val inDb = adPulsService.findByUuidAndType(uuid, PulsEventType.pageviews)
            assertNotNull(inDb!!.id)
            assertEquals(adPulsDTO.id, inDb.id)
            val empty = adPulsService.saveAll(listOf())
            assertEquals(0, empty.size)
            val savedList = adPulsService.saveAll(listOf(adPulsDTO, inDb))
            assertEquals(2, savedList.size)

            ctx.setRollbackOnly()
        }
    }
}
