package no.nav.arbeidsplassen.importapi.adstate

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.provider.info

class AdStateService(
    private val adStateRepository: AdStateRepository,
    private val objectMapper: ObjectMapper,
    private val providerService: ProviderService
) {

    @Throws(ImportApiError::class)
    fun getAdStateByUuid(uuid: String): AdStatePublicDTO {
        return adStateRepository.findByUuid(uuid)?.toDTO()
            ?: throw ImportApiError("AdState with $uuid not found", ErrorType.NOT_FOUND)
    }

    fun getAdStateByUuidAndProviderId(uuid: String, providerId: Long):
            AdStatePublicDTO = adStateRepository.findByUuidAndProviderId(uuid, providerId)?.toDTO()
        ?: throw ImportApiError("AdState with $uuid for provider $providerId not found", ErrorType.NOT_FOUND)

    fun getAdStatesByProviderReference(providerId: Long, reference: String): AdStatePublicDTO =
        adStateRepository.findByProviderIdAndReference(providerId, reference)?.toDTO()
            ?: throw ImportApiError("AdState with $providerId $reference not found", ErrorType.NOT_FOUND)


    fun resendAdState(uuid: String): AdStatePublicDTO {
        val resend = adStateRepository.findByUuid(uuid)?.copy(updated = LocalDateTime.now())
            ?: throw ImportApiError("Adstate with $uuid not found", ErrorType.NOT_FOUND)
        return adStateRepository.save(resend).toDTO()
    }

    private fun AdState.toDTO(): AdStatePublicDTO {
        return AdStatePublicDTO(
            uuid = uuid,
            versionId = versionId,
            reference = reference,
            ad = objectMapper.readValue(jsonPayload, AdDTO::class.java),
            updated = updated,
            created = created,
            providerId = providerId
        )
    }

    // Two DTO to differentiate between public and internal service to service use.
    private fun AdState.toInternalDTO(): AdStateDTO {
        return AdStateDTO(
            uuid = uuid,
            versionId = versionId,
            reference = reference,
            ad = objectMapper.readValue(jsonPayload, AdDTO::class.java),
            updated = updated,
            created = created,
            provider = providerService.findById(providerId).info()
        )
    }

    fun convertToInternalDto(adState: AdState) = adState.toInternalDTO()
}
