package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.Open
import jakarta.inject.Singleton

@Singleton
@Open
@CacheConfig("providers")
class ProviderService(private val providerRepository: ProviderRepository) {


    // the second parameter is used to cache invalidate, dont remove it.
    @CacheInvalidate(parameters = ["id"])
    fun save(dto: ProviderDTO, id: Long? = dto.id): ProviderDTO {
        return providerRepository.save(dto.toEntity()).toDTO()
    }

    @Cacheable
    fun findById(id:Long): ProviderDTO {
        return providerRepository.findById(id).orElseThrow().toDTO()
    }

    fun list(page: Pageable): Slice<ProviderDTO> {
        return providerRepository.list(page).map {
            it.toDTO()
        }
    }

    private fun ProviderDTO.toEntity(): Provider {
        return Provider(id = id, jwtid = jwtid, email = email, identifier = identifier, phone = phone)
    }

    private fun Provider.toDTO(): ProviderDTO {
        return ProviderDTO(id=id, jwtid = jwtid, email = email, identifier = identifier, phone = phone)
    }
}

