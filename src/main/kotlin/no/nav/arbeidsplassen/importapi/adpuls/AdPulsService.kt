package no.nav.arbeidsplassen.importapi.adpuls

import jakarta.inject.Singleton

@Singleton
class AdPulsService(private val repository: AdPulsRepository) {


    fun save(adPulsDTO: AdPulsDTO) {
        repository.save(repository.findByUuidAndType(adPulsDTO.uuid, adPulsDTO.type)?.copy(total = adPulsDTO.total) ?: adPulsDTO.toEntity())
    }

    private fun AdPuls.toDTO(): AdPulsDTO {
        return AdPulsDTO(id=id, providerId=providerId, reference=reference, uuid = uuid, type = type, total = total, created=created, updated=updated)
    }

    private fun AdPulsDTO.toEntity(): AdPuls {
        return AdPuls(id=id, providerId=providerId, reference=reference, uuid=uuid, type = type, total = total, created=created, updated=updated)
    }


}
