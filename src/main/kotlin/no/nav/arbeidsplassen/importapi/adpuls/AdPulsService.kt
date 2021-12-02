package no.nav.arbeidsplassen.importapi.adpuls

import jakarta.inject.Singleton
import java.time.LocalDateTime

@Singleton
class AdPulsService(private val repository: AdPulsRepository) {


    fun findByUuid(uuid: String): List<AdPulsDTO> {
        return repository.findByUuid(uuid).map{it.toDTO()}
    }

    fun findByProviderReference(providerId: Long, reference: String): List<AdPulsDTO> {
        return repository.findByProviderIdAndReference(providerId, reference).map { it.toDTO() }
    }

    fun save(adPulsDTO: AdPulsDTO): AdPulsDTO {
        return repository.save(repository.findByUuidAndType(adPulsDTO.uuid, adPulsDTO.type)?.copy(total = adPulsDTO.total)
            ?: adPulsDTO.toEntity()).toDTO()
    }

    fun saveAll(adPulsDTOs: List<AdPulsDTO>): List<AdPulsDTO> {
        return repository.saveAll(
            adPulsDTOs.map { repository.findByUuidAndType(it.uuid, it.type)?.copy(total = it.total) ?: it.toEntity()}
        ).map { it.toDTO() }
    }

    private fun AdPuls.toDTO(): AdPulsDTO {
        return AdPulsDTO(id=id, providerId=providerId, reference=reference, uuid = uuid, type = type, total = total, created=created, updated=updated)
    }

    private fun AdPulsDTO.toEntity(): AdPuls {
        return AdPuls(id=id, providerId=providerId, reference=reference, uuid=uuid, type = type, total = total, created=created, updated=updated)
    }

    fun findByProviderIdAndUpdatedAfter(providerId: Long, updatedAfter: LocalDateTime): List<AdPulsDTO> {
        return repository.findByProviderIdAndUpdatedAfter(providerId, updatedAfter).map { it.toDTO() }
    }

}
