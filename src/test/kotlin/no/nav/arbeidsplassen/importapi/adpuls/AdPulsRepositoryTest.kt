package no.nav.arbeidsplassen.importapi.adpuls

import java.util.UUID
import no.nav.arbeidsplassen.importapi.app.TestRepositories
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdPulsRepositoryTest : TestRepositories() {

    private val repository: AdPulsRepository = appCtx.databaseApplicationContext.adPulsRepository
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun readAndSave() {
        txTemplate.doInTransactionNullable { ctx ->

            val provider = providerRepository.newTestProvider()
            val first = repository.save(
                AdPuls(
                    providerId = provider.id!!,
                    uuid = UUID.randomUUID().toString(),
                    reference = UUID.randomUUID().toString(),
                    type = PulsEventType.pageviews,
                    total = 10
                )
            )
            assertNotNull(first.id)
            val inDb = repository.findById(first.id!!)!!
            assertNotNull(inDb)
            assertEquals(10, inDb.total)
            val new = inDb.copy(total = 20)
            repository.save(new)
            val typeUuid = repository.findByUuidAndType(first.uuid, first.type)
            assertNotNull(typeUuid)
            assertEquals(first.id, typeUuid!!.id)
            assertEquals(20, typeUuid.total)

            ctx.setRollbackOnly()
        }
    }
}
