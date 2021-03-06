package no.nav.arbeidsplassen.importapi.transferlog

import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.provider.info

import javax.inject.Singleton

@Singleton
class TransferLogService(private val transferLogRepository: TransferLogRepository) {


    fun existsByProviderIdAndMd5(providerId: Long, md5: String):
            Boolean = transferLogRepository.existsByProviderIdAndMd5(providerId, md5)

    fun save(dto: TransferLogDTO): TransferLogDTO {
        return transferLogRepository.save(dto.toEntity()).toDTO()
    }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): TransferLogDTO {
       return transferLogRepository.findByIdAndProviderId(versionId, providerId)
               .orElseThrow{ ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND) }
               .toDTO()
    }

    fun findByVersionId(versionId: Long): TransferLogDTO {
        return transferLogRepository.findById(versionId).orElseThrow {
            ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)
        }.toDTO()
    }

    private fun TransferLogDTO.toEntity(): TransferLog {
        return TransferLog(providerId = providerId, md5 = md5, payload = payload!!, items = items, message = message)
    }

    private fun TransferLog.toDTO(): TransferLogDTO {
        return TransferLogDTO(versionId = id!!, message = message, status = status,
                md5 = md5, created = created, updated = updated, payload = payload, items = items, providerId = providerId)
    }

    fun resend(versionId: Long):TransferLogDTO {
        val received = transferLogRepository.findById(versionId).orElseThrow {
            ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)
        }.copy(status = TransferLogStatus.RECEIVED)
        return transferLogRepository.save(received).toDTO()
    }

}
