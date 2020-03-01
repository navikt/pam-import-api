package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import no.nav.arbeidsplassen.importapi.Open
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dto.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.transaction.Transactional
import kotlin.streams.toList

@Singleton
@Open
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
        LOG.debug("received ${transferlogs.size}")
        transferlogs.stream().forEach {
            mapTransferLog(it)
        }
    }

    fun deleteTransferLogTask(date: LocalDateTime = LocalDateTime.now().minusMonths(deleteMonths)) {
        LOG.info("Deleting transferlog before $date")
        transferLogRepository.deleteByUpdatedBefore(date)
    }

    @Transactional
    fun mapTransferLog(it: TransferLog) {
        try {
            LOG.info("mapping transfer ${it.id} from provider ${it.providerId}")
            adStateRepository.saveAll(mapTransferLogs(it))
            transferLogRepository.save(it.copy(status = TransferLogStatus.DONE))
        } catch (e: Exception) {
            LOG.error("Got exception while handling transfer log ${it.id}", e)
            transferLogRepository.save(it.copy(status = TransferLogStatus.ERROR))
        }
    }

    private fun mapTransferLogs(transferLog: TransferLog): List<AdState> {
        val ads = objectMapper.readValue<List<AdDTO>>(transferLog.payload, object: TypeReference<List<AdDTO>>(){})
        return ads.stream().map { mapAdToAdState(it, transferLog)}.toList()

    }

    private fun mapAdToAdState(ad: AdDTO, transferLog: TransferLog): AdState {
        val inDb = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb.map { it.copy(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad)) }
                .orElse(AdState(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad),
                        providerId = transferLog.providerId, reference = ad.reference))
    }

}