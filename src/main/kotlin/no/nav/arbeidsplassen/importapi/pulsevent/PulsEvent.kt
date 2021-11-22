package no.nav.arbeidsplassen.importapi.pulsevent

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

@MappedEntity
data class PulsEvent(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    val type: String,
    val total: Long,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)
fun PulsEvent.isNew(): Boolean = id == null
