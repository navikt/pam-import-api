package no.nav.arbeidsplassen.importapi.adinfo

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class AdInfoDTO(
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    val activity: MutableMap<String, Any> = mutableMapOf(),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)
