package no.nav.arbeidsplassen.importapi.adinfo

import jakarta.inject.Singleton

@Singleton
class AdInfoService(private val repository: AdInfoRepository) {

    fun findByUuid(uuid: String): AdInfoDTO? {
        return repository.findByUuid(uuid)?.toDTO()
    }

    fun updatePulsEventTotal(dto: AdInfoDTO): AdInfoDTO {
        val event = repository.findByUuid(dto.uuid)?.let {
            updateEventData(it)
        } ?: dto.toEntity()
        return repository.save(event).toDTO()
    }

    private fun updateEventData(it: AdInfo): AdInfo {
        val data = it.data
        return it.copy()
    }

    private fun AdInfo.toDTO(): AdInfoDTO {
        return AdInfoDTO(id=id, providerId=providerId, reference=reference, uuid = uuid, data=data, created=created, updated=updated)
    }

    private fun AdInfoDTO.toEntity(): AdInfo {
        return AdInfo(id=id, providerId=providerId, reference=reference, uuid=uuid, data=data, created=created, updated=updated)
    }


}
