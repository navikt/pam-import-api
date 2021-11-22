package no.nav.arbeidsplassen.importapi.pulsevent

import jakarta.inject.Singleton

@Singleton
class PulsEventService(private val repository: PulsEventRepository) {

    fun findByUuidAndType(uuid: String, type: String): PulsEventDTO? {
        return repository.findByUuidAndType(uuid, type)?.toDTO()
    }

    fun updatePulsEventTotal(dto: PulsEventDTO): PulsEventDTO {
        val event = repository.findByUuidAndType(dto.uuid, dto.type)?.let {
            it.copy(total = it.total + dto.total)
        } ?: dto.toEntity()
        val saved = repository.save(event).toDTO()
        return saved
    }

    private fun PulsEvent.toDTO(): PulsEventDTO {
        return PulsEventDTO(id = id, uuid = uuid , total = total, type=type, created=created, updated=updated)
    }

    private fun PulsEventDTO.toEntity(): PulsEvent {
        return PulsEvent(id=id, uuid=uuid, total=total, type=type, created=created, updated=updated)
    }

}
