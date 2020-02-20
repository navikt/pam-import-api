package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.aop.Around
import io.micronaut.data.model.Pageable
import no.nav.arbeidsplassen.importapi.dao.AdState
import no.nav.arbeidsplassen.importapi.dao.AdStateRepository
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Payload
import kotlin.streams.toList

@Singleton
@Around
class TransferLogService(private val transferLogRepository: TransferLogRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogService::class.java)
    }

    @Transactional
    fun existsByProviderIdAndMd5(providerId: Long, md5: String):
            Boolean = transferLogRepository.existsByProviderIdAndMd5(providerId, md5)

    @Transactional
    fun saveTransfer(dto: TransferDTO, md5: String, payload: String): TransferLogDTO {
        return transferLogRepository.save(dto.toEntity(md5, payload)).toDTO()
    }

    @Transactional
    fun findByVersionId(versionId:Long): TransferLogDTO {
       return transferLogRepository.findById(versionId).orElseThrow().toDTO()
    }

    private fun TransferDTO.toEntity(md5:String, payload: String): TransferLog {
        return TransferLog(providerId = provider.id!!, md5 = md5, payload = payload)
    }

    private fun TransferLog.toDTO(): TransferLogDTO {
        return TransferLogDTO(versionId = id!!, message = message, status = status.name, md5 = md5, created = created, updated = updated)
    }
}