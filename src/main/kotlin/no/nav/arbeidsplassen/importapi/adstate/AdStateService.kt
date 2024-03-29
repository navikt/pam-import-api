package no.nav.arbeidsplassen.importapi.adstate

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.provider.info
import java.time.LocalDateTime

@Singleton
class AdStateService(private val adStateRepository: AdStateRepository,
                     private val objectMapper: ObjectMapper,
                     private val providerService: ProviderService) {

    fun getAdStates(pageable: Pageable): Slice<AdStatePublicDTO> {
        return adStateRepository.list(pageable).map {
            it.toDTO()
        }
    }

    @Throws(ImportApiError::class)
    fun getAdStateByUuid(uuid: String): AdStatePublicDTO {
        return adStateRepository.findByUuid(uuid)?.toDTO()
            ?: throw ImportApiError("AdState with $uuid not found", ErrorType.NOT_FOUND) }

    fun getAdStateByUuidAndProviderId(uuid: String, providerId: Long):
            AdStatePublicDTO = adStateRepository.findByUuidAndProviderId(uuid, providerId)
            .orElseThrow{ ImportApiError("AdState with $uuid for provider $providerId not found", ErrorType.NOT_FOUND) }.toDTO()

    fun getAdStatesByProviderReference(providerId:Long, reference:String): AdStatePublicDTO =
        adStateRepository.findByProviderIdAndReference(providerId, reference)
                .orElseThrow { ImportApiError("AdState with $providerId $reference not found", ErrorType.NOT_FOUND) }
                .toDTO()

    fun getAdStatesByUpdatedForInternalUse(updated:LocalDateTime, pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.findByUpdatedGreaterThanEquals(updated, pageable).map {
            it.toInternalDTO()
        }
    }

    fun getAdStatesByVersionId(versionId: Long, pageable: Pageable): Slice<AdStatePublicDTO> {
        return adStateRepository.list(versionId, pageable).map{
            it.toDTO()
        }
    }

    fun getAdStatesByVersionIdAndProviderId(versionId: Long, providerId: Long, pageable: Pageable): Slice<AdStatePublicDTO> {
        return adStateRepository.list(versionId, providerId, pageable).map{
            it.toDTO()
        }
    }

    fun resendAdState(uuid: String): AdStatePublicDTO {
        val resend = adStateRepository.findByUuid(uuid)?.copy(updated = LocalDateTime.now()) ?: throw
           ImportApiError("Adstate with $uuid not found", ErrorType.NOT_FOUND)
        return adStateRepository.save(resend).toDTO()
    }

    private fun AdState.toDTO(): AdStatePublicDTO {
        return AdStatePublicDTO(uuid = uuid, versionId = versionId, reference = reference,
                ad = objectMapper.readValue(jsonPayload, AdDTO::class.java), updated = updated, created = created, providerId = providerId)
    }

    // Two DTO to differentiate between public and internal service to service use.
    private fun AdState.toInternalDTO(): AdStateDTO {
        return AdStateDTO(uuid = uuid, versionId = versionId, reference = reference,
                ad = objectMapper.readValue(jsonPayload, AdDTO::class.java), updated = updated, created = created, provider = providerService.findById(providerId).info())
    }

    fun convertToInternalDto(adState: AdState) = adState.toInternalDTO()
}
