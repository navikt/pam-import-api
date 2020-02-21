package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.aop.Around
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

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