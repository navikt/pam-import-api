package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class AdAdminStatusRepositoryTest(private val adAdminStatusRepository: AdAdminStatusRepository,
                                  private val providerRepository: ProviderRepository) {

    @Test
    fun adAdminStatusCRUDTest() {
        val provider = Provider(email = "test@test.test", username = "tester")
        val providerinDB = providerRepository.save(provider)
        val adminStatus = AdAdminStatus(message = "Diskriminerende", reference = "12345", providerId = providerinDB.id!!)
        val created = adAdminStatusRepository.save(adminStatus)
        assertNotNull(created.id)
        val read = adAdminStatusRepository.findById(created.id!!).get()
        assertNotNull(read)
        assertEquals("12345", read.reference)
        val update = read.copy(message = null, status = Status.ACTIVE)
        val updated = adAdminStatusRepository.save(update)
        assertEquals(created.id!!, updated.id!!)
        assertNull(updated.message)
        assertEquals(Status.ACTIVE, updated.status)
        println(updated)
        adAdminStatusRepository.deleteById(updated.id!!)
        val deleted = adAdminStatusRepository.findById(created.id!!)
        assertTrue(deleted.isEmpty)
        val adminStatus2 = AdAdminStatus(message = "Diskriminerende", reference = "54321", providerId = providerinDB.id!!)
        val adminStatuses = mutableListOf(adminStatus, adminStatus2)
        adAdminStatusRepository.saveAll(adminStatuses)
        assertEquals(2, adAdminStatusRepository.findAll().count())
    }
}