package no.nav.arbeidsplassen.importapi.provider


import no.nav.arbeidsplassen.importapi.app.TestRepositories
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProviderRepositoryTest : TestRepositories() {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ProviderRepositoryTest::class.java)
    }

    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val txTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun providerCRUDTest() {
        txTemplate.doInTransactionNullable { ctx ->
            val provider = providerRepository.newTestProvider()
            assertNotNull(provider.id)
            val id = provider.id!!
            val read = providerRepository.findById(id)
            assertNotNull(read)
            assertEquals(provider.email, read!!.email)
            assertEquals(provider.identifier, read.identifier)
            val update = read.copy(email = "updated@test.test")
            val updated = providerRepository.save(update)
            assertEquals("updated@test.test", updated.email)
            LOG.info(updated.toString())
            providerRepository.deleteById(id)
            val deleted = providerRepository.findById(provider.id!!)
            Assertions.assertNull(deleted)
            val provider2 = Provider(email = "test2@test.test", identifier = "tester2", phone = "12345678")
            val provider3 = Provider(email = "test3@test.test", identifier = "tester3", phone = "12345678")
            val providers = listOf(provider2, provider3)
            providers.forEach { providerRepository.save(it) }

            ctx.setRollbackOnly()
        }
    }

}
