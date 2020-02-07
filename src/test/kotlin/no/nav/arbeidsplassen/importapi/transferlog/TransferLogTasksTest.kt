package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.TransferLogTasks
import no.nav.arbeidsplassen.importapi.dao.*
import no.nav.arbeidsplassen.importapi.dto.DTOValidation
import no.nav.arbeidsplassen.importapi.md5Hex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class TransferLogTasksTest(private val transferLogTasks: TransferLogTasks,
                           private val transferLogRepository: TransferLogRepository,
                           private val providerRepository: ProviderRepository,
                           private val objectMapper: ObjectMapper,
                           private val dtoValidation: DTOValidation,
                           private val adStateRepository: AdStateRepository) {

    @Test
    fun doTransferLogTaskTest() {
        val json = dtoValidation.jsonToNode(TransferLogTasksTest::class.java.getResourceAsStream("/transfer-ads.json"))
        val payload = objectMapper.writeValueAsString(json)
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(TransferLog(providerId = provider.id!!, md5 = payload.md5Hex(), payload = payload))
        transferLogTasks.doTransferLogTask()
        val adstates = adStateRepository.findAll()
        assertEquals(2, adstates.count())
        val transferLog = transferLogRepository.findByStatus(TransferLogStatus.DONE, Pageable.from(0))
        assertEquals(1, transferLog.count())
    }
}