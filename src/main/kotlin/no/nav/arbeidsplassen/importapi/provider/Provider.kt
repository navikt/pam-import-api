package no.nav.arbeidsplassen.importapi.provider

import java.time.LocalDateTime
import java.util.UUID
import no.nav.arbeidsplassen.importapi.repository.Entity

data class Provider(
    override var id: Long? = null,
    val jwtid: String = UUID.randomUUID().toString(),
    val identifier: String,
    val email: String,
    val phone: String,
    val updated: LocalDateTime = LocalDateTime.now(),
    val created: LocalDateTime = LocalDateTime.now()
) : Entity
