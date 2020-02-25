package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.ImportApiError
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import javax.inject.Singleton

@Singleton
class AdAdminStatusService(private val adAdminStatusRepository: AdAdminStatusRepository) {

    fun findByProviderReference(providerId: Long, reference: String): AdAdminStatusDTO {
        return adAdminStatusRepository.findByProviderIdAndReference(providerId, reference)
                .orElseThrow{
                    ImportApiError(message = "AdAdminStatus for $providerId, $reference not found", type = ErrorType.NOT_FOUND)
                }
                .toDTO()
    }

    fun findByVersion(versionId: Long): List<AdAdminStatusDTO> {
        return adAdminStatusRepository.findByVersionId(versionId).map {
            it.toDTO()
        }
    }

    private fun AdAdminStatus.toDTO(): AdAdminStatusDTO {
        return AdAdminStatusDTO(uuid = uuid, providerId = providerId, reference = reference, created = created,
                updated = updated, status = status.name, message = message )
    }

}