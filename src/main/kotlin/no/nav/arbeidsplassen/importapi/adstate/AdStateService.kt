package no.nav.arbeidsplassen.importapi.adstate

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import no.nav.arbeidsplassen.importapi.ApiError
import no.nav.arbeidsplassen.importapi.ErrorType
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class AdStateService(private val adStateRepository: AdStateRepository,
                     private val objectMapper: ObjectMapper) {

    fun getAdStates(pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.list(pageable).map {
            it.toDTO()
        }
    }

    fun getAdStateByUuid(uuid: UUID):
            AdStateDTO = adStateRepository.findByUuid(uuid)
            .orElseThrow{ApiError("AdState with $uuid not found", ErrorType.NOT_FOUND)}
            .toDTO()

    fun getAdStatesByProviderReference(providerId:Long, reference:String): AdStateDTO =
        adStateRepository.findByProviderIdAndReference(providerId, reference)
                .orElseThrow { ApiError("AdState with $providerId $reference not found", ErrorType.NOT_FOUND) }
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

    private fun AdState.toDTO(): AdStateDTO {
        return AdStateDTO(uuid = uuid, versionId = versionId, providerId = providerId, reference = reference,
                ad = objectMapper.readValue(jsonPayload, AdDTO::class.java), updated = updated, created = created)
    }

}