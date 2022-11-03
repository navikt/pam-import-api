package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dao.*
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.toMD5Hex
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class TransferLogTasksTest(private val transferLogTasks: TransferLogTasks,
                           private val transferLogRepository: TransferLogRepository,
                           private val providerRepository: ProviderRepository,
                           private val objectMapper: ObjectMapper,
                           private val adStateRepository: AdStateRepository) {

    @Test
    fun doTransferLogTaskTest() {
        val payload = objectMapper.transferJsonString()
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = payload.toMD5Hex(), payload = payload, items =  2))
        transferLogTasks.processTransferLogTask()
        val adstates = adStateRepository.findAll()
        adstates.forEach { println(it.jsonPayload) }
        assertEquals(2, adstates.count())
        val transferLog = transferLogRepository.findByStatus(TransferLogStatus.DONE, Pageable.from(0))
        assertEquals(1, transferLog.count())
    }

    @Test
    fun deleteTransferLogTaskTest() {
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = "1a2b3c4d5e", payload = "payload", items = 1))
        val date = LocalDateTime.now().plusMonths(7)
        transferLogTasks.deleteTransferLogTask(date)
        assertEquals(0, transferLogRepository.findAll().count())
    }

    @Test
    fun replaceAmpersandsTest() {
        var result = transferLogTasks.sanitizeAd(AdDTO(
            adText = "AdText &amp;",
            employer = EmployerDTO(businessName = "BusinessName &amp;", location = LocationDTO(), orgnr = "orgnr &amp;", reference = "Reference &amp;"),
            expires = LocalDateTime.now().plusMonths(1),
            published = LocalDateTime.now(),
            reference = "Reference &amp;",
            title = "Title &amp;"
        ))
        assertEquals("Title &", result.title)
        assertEquals("BusinessName &", result.employer?.businessName)
        assertEquals("AdText &amp;", result.adText) // HTML felt skal ikke endres
    }

    @Test
    fun replaceAmpersandsHandleNullValues() {
        var result = transferLogTasks.sanitizeAd(AdDTO(
            adText = "?",
            employer = null,
            expires = null,
            published = null,
            reference = "?",
            title = "?",
            locationList = listOf(LocationDTO())
        ))
        assertNotNull(result)
    }
}
