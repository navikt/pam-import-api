package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.ImportApiError
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import java.util.*
import javax.inject.Singleton

@Singleton
class AdminStatusService(private val adminStatusRepository: AdminStatusRepository) {

    fun findByProviderReference(providerId: Long, reference: String): AdAdminStatusDTO {
        return adminStatusRepository.findByProviderIdAndReference(providerId, reference)
                .orElseThrow{
                    ImportApiError(message = "AdAdminStatus for $providerId, $reference not found", type = ErrorType.NOT_FOUND)
                }
                .toDTO()
    }

    fun findByVersion(versionId: Long): List<AdAdminStatusDTO> {
        return adminStatusRepository.findByVersionId(versionId).map {
            it.toDTO()
        }
    }

    fun findByVersionAndProviderId(versionId: Long, providerId: Long): List<AdAdminStatusDTO> {
        return adminStatusRepository.findByVersionIdAndProviderId(versionId, providerId).map {
            it.toDTO()
        }
    }

    private fun AdminStatus.toDTO(): AdAdminStatusDTO {
        return AdAdminStatusDTO(uuid = uuid, providerId = providerId, reference = reference, created = created,
                updated = updated, status = status, message = message )
    }

}