package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import no.nav.arbeidsplassen.importapi.dao.*
import no.nav.arbeidsplassen.importapi.dto.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class TransferLogTasks(private val transferLogRepository: TransferLogRepository,
                       private val adStateRepository: AdStateRepository,
                       private val objectMapper: ObjectMapper,
                       @Value("\${transferlog.size:50}") private val logSize: Int,
                       @Value("\${transferlog.delete.months:6}") private val deleteMonths: Long) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogTasks::class.java)
    }

    fun doTransferLogTask() {
        val transferlogs = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED, Pageable.from(0,logSize))
        transferlogs.stream().forEach {
            try {
                LOG.info("Received a transfer from provider ${it.providerId}")
                adStateRepository.saveAll(mapTransferLogs(it))
                transferLogRepository.save(it.copy(status=TransferLogStatus.DONE))
            }
            catch (e: Exception) {
                LOG.error("Got exception while handling transfer log ${it.id}")
                transferLogRepository.save(it.copy(status=TransferLogStatus.ERROR))
            }
        }
    }

    fun deleteTransferLogTask(date: LocalDateTime = LocalDateTime.now().minusMonths(deleteMonths)) {
        LOG.info("Deleting transferlog before $date")
        transferLogRepository.deleteByUpdatedBefore(date)
    }

    private fun mapTransferLogs(transferLog: TransferLog): List<AdState> {
        val transferDTO = objectMapper.readValue(transferLog.payload, Transfer::class.java)
        return transferDTO.ads.stream().map { mapAdToAdState(it, transferLog)}.toList()

    }

    private fun mapAdToAdState(ad: Ad, transferLog: TransferLog): AdState {
        val inDb = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb.map { it.copy(transferVersion = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad)) }
                .orElse(AdState(transferVersion = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad),
                        providerId = transferLog.providerId, reference = ad.reference))
    }

}