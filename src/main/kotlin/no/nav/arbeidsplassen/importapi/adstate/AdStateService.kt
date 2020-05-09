package no.nav.arbeidsplassen.importapi.adstate

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.provider.info
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class AdStateService(private val adStateRepository: AdStateRepository,
                     private val objectMapper: ObjectMapper,
                     private val providerService: ProviderService) {

    fun getAdStates(pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.list(pageable).map {
            it.toDTO()
        }
    }

    fun getAdStateByUuid(uuid: String):
            AdStateDTO = adStateRepository.findByUuid(uuid)
            .orElseThrow{ ImportApiError("AdState with $uuid not found", ErrorType.NOT_FOUND) }
            .toDTO()

    fun getAdStateByUuidAndProviderId(uuid: String, providerId: Long):
            AdStateDTO = adStateRepository.findByUuidAndProviderId(uuid, providerId)
            .orElseThrow{ ImportApiError("AdState with $uuid for provider $providerId not found", ErrorType.NOT_FOUND) }.toDTO()

    fun getAdStatesByProviderReference(providerId:Long, reference:String): AdStateDTO =
        adStateRepository.findByProviderIdAndReference(providerId, reference)
                .orElseThrow { ImportApiError("AdState with $providerId $reference not found", ErrorType.NOT_FOUND) }
                .toDTO()

    fun getAdStatesByUpdated(updated:LocalDateTime, pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.findByUpdatedGreaterThanEquals(updated, pageable).map {
            it.toDTO()
        }
    }

    fun getAdStatesByVersionId(versionId: Long, pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.list(versionId, pageable).map{
            it.toDTO()
        }
    }

    fun getAdStatesByVersionIdAndProviderId(versionId: Long, providerId: Long, pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.list(versionId, providerId, pageable).map{
            it.toDTO()
        }
    }

    private fun AdState.toDTO(): AdStateDTO {
        return AdStateDTO(uuid = uuid, versionId = versionId, provider = providerService.findById(providerId).info(), reference = reference,
                ad = objectMapper.readValue(jsonPayload, AdDTO::class.java), updated = updated, created = created)
    }

}
