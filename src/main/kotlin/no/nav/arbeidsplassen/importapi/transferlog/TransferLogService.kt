package no.nav.arbeidsplassen.importapi.transferlog

import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService

import javax.inject.Singleton

@Singleton
class TransferLogService(private val transferLogRepository: TransferLogRepository,
                         private val providerService: ProviderService) {


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

    private fun TransferLogDTO.toEntity(): TransferLog {
        return TransferLog(providerId = provider.id!! , md5 = md5, payload = payload!!, items = items)
    }

    private fun TransferLog.toDTO(): TransferLogDTO {
        return TransferLogDTO(versionId = id!!, provider = providerService.findById(providerId) , message = message, status = status,
                md5 = md5, created = created, updated = updated, payload = payload, items = items)
    }
}