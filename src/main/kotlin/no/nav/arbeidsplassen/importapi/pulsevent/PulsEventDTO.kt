package no.nav.arbeidsplassen.importapi.pulsevent

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import java.time.LocalDateTime

@Introspected
data class PulsEventDTO(
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    val type: String,
    val total: Long,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)
