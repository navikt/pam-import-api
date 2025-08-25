package no.nav.arbeidsplassen.importapi.adpuls

import java.time.Instant

data class PulsEventDTO(
    var id: Long? = null,
    val oid: String,
    val total: Long = 1,
    val type: String,
    val properties: Map<String, Any> = emptyMap(),
    val created: Instant = Instant.now(),
    val updated: Instant = Instant.now()
)
