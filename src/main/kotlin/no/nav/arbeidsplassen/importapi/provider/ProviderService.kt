package no.nav.arbeidsplassen.importapi.provider

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.dao.Provider
import no.nav.arbeidsplassen.importapi.dao.ProviderRepository
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import java.util.*
import javax.inject.Singleton

@Singleton
class ProviderService(private val providerRepository: ProviderRepository) {


    fun save(dto: ProviderDTO): ProviderDTO {
        return toDTO(providerRepository.save(toEntity(dto)))
    }

    fun findByUuid(uuid: UUID): ProviderDTO {
       return toDTO(providerRepository.findByUuid(uuid).orElseThrow())
    }

    private fun toEntity(dto: ProviderDTO): Provider {
        check(dto.email!=null && dto.userName!=null)
        return Provider(id=dto.id, email = dto.email, username = dto.userName, uuid = dto.uuid)
    }

    private fun toDTO(entity: Provider): ProviderDTO {
        return ProviderDTO(id=entity.id, email = entity.email, userName = entity.username, uuid = entity.uuid)
    }
}