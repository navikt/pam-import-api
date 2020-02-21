package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.aop.Around
import no.nav.arbeidsplassen.importapi.ApiError
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.dto.TransferDTO
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Around
class TransferLogService(private val transferLogRepository: TransferLogRepository) {

    @Transactional
    fun existsByProviderIdAndMd5(providerId: Long, md5: String):
            Boolean = transferLogRepository.existsByProviderIdAndMd5(providerId, md5)

    @Transactional
    fun saveTransfer(dto: TransferDTO, md5: String, payload: String): TransferLogDTO {
        return transferLogRepository.save(dto.toEntity(md5, payload)).toDTO()
    }

    @Transactional
    fun findByVersionId(versionId:Long): TransferLogDTO {
       return transferLogRepository.findById(versionId)
               .orElseThrow{ApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)}
               .toDTO()
    }

    private fun TransferDTO.toEntity(md5:String, payload: String): TransferLog {
        return TransferLog(providerId = provider.id!!, md5 = md5, payload = payload)
    }

    private fun TransferLog.toDTO(): TransferLogDTO {
        return TransferLogDTO(versionId = id!!, message = message, status = status.name, md5 = md5, created = created, updated = updated)
    }
}