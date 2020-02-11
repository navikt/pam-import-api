package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.aop.Around
import io.micronaut.data.model.Pageable
import no.nav.arbeidsplassen.importapi.dao.AdState
import no.nav.arbeidsplassen.importapi.dao.AdStateRepository
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.transaction.Transactional
import kotlin.streams.toList

@Singleton
@Around
class TransferLogService(private val adStateRepository: AdStateRepository,
                         private val transferLogRepository: TransferLogRepository,
                         private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogService::class.java)
    }

    @Transactional
    fun mapTransferLog(it: TransferLog) {
        try {
            LOG.info("mapping transfer $it.id from provider ${it.providerId}")
            adStateRepository.saveAll(mapTransferLogs(it))
            transferLogRepository.save(it.copy(status = TransferLogStatus.DONE))
        } catch (e: Exception) {
            LOG.error("Got exception while handling transfer log ${it.id}", e)
            transferLogRepository.save(it.copy(status = TransferLogStatus.ERROR))
        }
    }

    @Transactional
    fun findByStatus(status: TransferLogStatus, pageable: Pageable): List<TransferLog> {
        return transferLogRepository.findByStatus(status, pageable)
    }

    @Transactional
    fun deleteByUpdatedBefore(date: LocalDateTime) {
        transferLogRepository.deleteByUpdatedBefore(date)
    }

    private fun mapTransferLogs(transferLog: TransferLog): List<AdState> {
        val transferDTO = objectMapper.readValue(transferLog.payload, TransferDTO::class.java)
        return transferDTO.ads.stream().map { mapAdToAdState(it, transferLog)}.toList()

    }

    private fun mapAdToAdState(ad: AdDTO, transferLog: TransferLog): AdState {
        val inDb = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb.map { it.copy(transferVersion = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad)) }
                .orElse(AdState(transferVersion = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(ad),
                        providerId = transferLog.providerId, reference = ad.reference))
    }


}