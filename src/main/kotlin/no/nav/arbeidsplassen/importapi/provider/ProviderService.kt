package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import javax.inject.Singleton

@Singleton
class ProviderService(private val providerRepository: ProviderRepository) {


    fun save(dto: ProviderDTO): ProviderDTO {
        return providerRepository.save(dto.toEntity()).toDTO()
    }

    fun findById(id:Long): ProviderDTO {
        return providerRepository.findById(id).orElseThrow().toDTO()
    }

    fun list(page: Pageable): Slice<ProviderDTO> {
        return providerRepository.list(page).map {
            it.toDTO()
        }
    }

    private fun ProviderDTO.toEntity(): Provider {
        return Provider(id = id, email = email, identifier = identifier)
    }

    private fun Provider.toDTO(): ProviderDTO {
        return ProviderDTO(id=id, email = email, identifier = identifier)
    }
}