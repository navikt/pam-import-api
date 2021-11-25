package no.nav.arbeidsplassen.importapi.adinfo

import jakarta.inject.Singleton

@Singleton
class AdInfoService(private val repository: AdInfoRepository) {

    fun findByUuid(uuid: String): AdInfoDTO? {
        return repository.findByUuid(uuid)?.toDTO()
    }


    private fun AdInfo.toDTO(): AdInfoDTO {
        return AdInfoDTO(id=id, providerId=providerId, reference=reference, uuid = uuid, activity=activity, created=created, updated=updated)
    }

    private fun AdInfoDTO.toEntity(): AdInfo {
        return AdInfo(id=id, providerId=providerId, reference=reference, uuid=uuid, activity=activity, created=created, updated=updated)
    }


}
