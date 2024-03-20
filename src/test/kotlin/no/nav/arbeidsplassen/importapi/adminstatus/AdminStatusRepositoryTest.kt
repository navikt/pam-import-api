package no.nav.arbeidsplassen.importapi.adminstatus

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.PublishStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.Status
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AdminStatusRepositoryTest(private val adminStatusRepository: AdminStatusRepository,
                                private val providerRepository: ProviderRepository,
                                private val transferLogRepository: TransferLogRepository) {

    @Test
    fun adAdminStatusCRUDTest() {
        val provider = providerRepository.newTestProvider()
        val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring", items = 1)
        val transferInDb = transferLogRepository.save(transferLog)
        val adminStatus = AdminStatus(message = "Diskriminerende", reference = "12345", providerId = provider.id!!,
                versionId = transferInDb.id!!, uuid = UUID.randomUUID().toString())
        val created = adminStatusRepository.save(adminStatus)
        assertNotNull(created.id)
        val read = adminStatusRepository.findById(created.id!!).get()
        assertNotNull(read)
        assertEquals("12345", read.reference)
        val update = read.copy(message = null, status = Status.DONE, publishStatus = PublishStatus.REJECTED)
        val updated = adminStatusRepository.save(update)
        assertEquals(created.id!!, updated.id!!)
        assertNull(updated.message)
        assertEquals(Status.DONE, updated.status)
        assertEquals(PublishStatus.REJECTED, updated.publishStatus)
        println(updated)
        adminStatusRepository.deleteById(updated.id!!)
        val deleted = adminStatusRepository.findById(created.id!!)
        assertTrue(deleted.isEmpty)
        val adminStatus2 = AdminStatus(message = "Diskriminerende", reference = "54321", providerId = provider.id!!,
                versionId = transferInDb.id!!, uuid = UUID.randomUUID().toString())
        val adminStatuses = listOf(adminStatus, adminStatus2)
        adminStatusRepository.saveAll(adminStatuses)
        assertEquals(2, adminStatusRepository.findAll().count())
    }
}
