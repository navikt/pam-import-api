package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.transaction.Transactional

@Open
@Singleton
class AdPulsService(private val repository: AdPulsRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsService::class.java)
    }

    fun findByUuid(uuid: String): List<AdPulsDTO> {
        return repository.findByUuid(uuid).map{it.toDTO()}
    }

    fun findByUuidAndType(uuid: String, type: PulsEventType): AdPulsDTO? {
        return repository.findByUuidAndType(uuid, type)?.toDTO()
    }

    fun findByProviderReference(providerId: Long, reference: String): List<AdPulsDTO> {
        return repository.findByProviderIdAndReference(providerId, reference).map { it.toDTO() }
    }

    fun save(adPulsDTO: AdPulsDTO): AdPulsDTO {
        return repository.save(repository.findByUuidAndType(adPulsDTO.uuid, adPulsDTO.type)?.copy(total = adPulsDTO.total)
            ?: adPulsDTO.toEntity()).toDTO()
    }


    fun saveAll(adPulsDTOs: List<AdPulsDTO>): List<AdPulsDTO> {
        return repository.saveAll(adPulsDTOs.map {
            repository.findByUuidAndType(it.uuid, it.type)?.copy(total = it.total) ?: it.toEntity()
        }).map { it.toDTO() }
    }

    private fun AdPuls.toDTO(): AdPulsDTO {
        return AdPulsDTO(id=id, providerId=providerId, reference=reference, uuid = uuid, type = type, total = total, created=created, updated=updated)
    }

    private fun AdPulsDTO.toEntity(): AdPuls {
        return AdPuls(id=id, providerId=providerId, reference=reference, uuid=uuid, type = type, total = total, created=created, updated=updated)
    }

    @Cacheable("provider_ad_stats")
    fun findByProviderIdAndUpdatedAfter(providerId: Long, updatedAfter: LocalDateTime): List<AdPulsDTO> {
        LOG.info("Getting puls events for provider $providerId and updated after $updatedAfter")
        return repository.findByProviderIdAndUpdatedAfter(providerId, updatedAfter).map { it.toDTO() }
    }

}
