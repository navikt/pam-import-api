package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.aop.Around
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Around
class ProviderService(private val providerRepository: ProviderRepository) {


    @Transactional
    fun save(dto: ProviderDTO): ProviderDTO {
        return toDTO(providerRepository.save(toEntity(dto)))
    }

    @Transactional
    fun findByUuid(uuid: UUID): ProviderDTO {
       return toDTO(providerRepository.findByUuid(uuid).orElseThrow())
    }

    @Transactional
    fun list(page: Pageable): Slice<ProviderDTO> {
        return providerRepository.list(page).map {
            toDTO(it)
        }
    }

    private fun toEntity(dto: ProviderDTO): Provider {
        check(dto.email!=null && dto.userName!=null)
        return Provider(id = dto.id, email = dto.email, username = dto.userName, uuid = dto.uuid)
    }

    private fun toDTO(entity: Provider): ProviderDTO {
        return ProviderDTO(id=entity.id, email = entity.email, userName = entity.username, uuid = entity.uuid)
    }
}