package no.nav.arbeidsplassen.importapi.provider

class ProviderService(
    private val providerRepository: ProviderRepository,
    private val providerCache: ProviderCache,
) {
    fun save(dto: ProviderDTO, id: Long? = dto.id): ProviderDTO {
        providerCache.invalidate(id)
        return providerRepository.save(dto.toEntity()).toDTO()
    }

    fun findAll() : List<ProviderDTO> = providerRepository.findAll().map { it.toDTO() }

    fun findById(id: Long): ProviderDTO {
        return providerCache.get(id)
            ?: providerRepository.findById(id)!!.toDTO()
                .also { providerCache.set(id, it) }
    }

    private fun ProviderDTO.toEntity(): Provider {
        return Provider(id = id, jwtid = jwtid, email = email, identifier = identifier, phone = phone)
    }

    private fun Provider.toDTO(): ProviderDTO {
        return ProviderDTO(id = id, jwtid = jwtid, email = email, identifier = identifier, phone = phone)
    }
}
