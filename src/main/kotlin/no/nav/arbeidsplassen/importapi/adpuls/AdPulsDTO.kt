package no.nav.arbeidsplassen.importapi.adpuls

import java.time.LocalDateTime

data class AdPulsDTO(
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    val type: PulsEventType,
    val total: Long,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)
