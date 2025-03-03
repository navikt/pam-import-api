package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.context.annotation.Value
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import jakarta.inject.Singleton

@Singleton
class AdminStatusService(private val adminStatusRepository: AdminStatusRepository,
                         @Value("\${ad.preview.url}") private val previewUrl: String) {

    fun findByProviderReference(providerId: Long, reference: String): AdAdminStatusDTO {
        return adminStatusRepository.findByProviderIdAndReference(providerId, reference)?.toDTO()
            ?: throw ImportApiError(message = "AdAdminStatus for $providerId, $reference not found", type = ErrorType.NOT_FOUND)
    }

    fun findByVersionAndProviderId(versionId: Long, providerId: Long): List<AdAdminStatusDTO> {
        return adminStatusRepository.findByVersionIdAndProviderId(versionId, providerId).map {
            it.toDTO()
        }
    }

    fun findByUuid(uuid: String): AdAdminStatusDTO {
        return adminStatusRepository.findByUuid(uuid)?.toDTO()
            ?: throw ImportApiError(message = "AdAdminStatus for $uuid not found", type = ErrorType.NOT_FOUND)
    }

    private fun AdminStatus.toDTO(): AdAdminStatusDTO {
        return AdAdminStatusDTO(uuid = uuid, providerId = providerId, reference = reference, created = created,
                url = "$previewUrl/$uuid",updated = updated, status = status, message = message, publishStatus = publishStatus)
    }

}
