package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.dto.AdStateDTO
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class AdStateService(private val adStateRepository: AdStateRepository ) {

    fun getAdStates(pageable: Pageable): Slice<AdStateDTO> {
        return adStateRepository.list(pageable).map {
            it.toDTO()
        }
    }

    fun getAdStatesByProviderReference(providerId:Long, reference:String): AdStateDTO {
        return adStateRepository.findByProviderIdAndReference(providerId, reference).orElseThrow().toDTO()
    }

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
                jsonPayload = jsonPayload, updated = updated, created = created)
    }

}