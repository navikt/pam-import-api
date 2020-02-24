package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dao.*
import no.nav.arbeidsplassen.importapi.dto.DTOValidation
import no.nav.arbeidsplassen.importapi.md5Hex
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class TransferLogTasksTest(private val transferLogTasks: TransferLogTasks,
                           private val transferLogRepository: TransferLogRepository,
                           private val providerRepository: ProviderRepository,
                           private val objectMapper: ObjectMapper,
                           private val dtoValidation: DTOValidation,
                           private val adStateRepository: AdStateRepository) {

    @Test
    fun doTransferLogTaskTest() {
        val json = TransferLogTasksTest::class.java.getResourceAsStream("/transfer-ads.json").bufferedReader().use { it.readText() }
        val payload = objectMapper.writeValueAsString(json)
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = payload.md5Hex(), payload = payload))
        transferLogTasks.doTransferLogTask()
        val adstates = adStateRepository.findAll()
        assertEquals(2, adstates.count())
        val transferLog = transferLogRepository.findByStatus(TransferLogStatus.DONE, Pageable.from(0))
        assertEquals(1, transferLog.count())
    }

    @Test
    fun deleteTransferLogTaskTest() {
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = "1a2b3c4d5e", payload = "payload"))
        val date = LocalDateTime.now().plusMonths(7)
        transferLogTasks.deleteTransferLogTask(date)
        assertEquals(0, transferLogRepository.findAll().count())
    }

}