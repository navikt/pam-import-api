package no.nav.arbeidsplassen.importapi.provider

import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
data class Provider(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val jwtid: String = UUID.randomUUID().toString(),
        val identifier: String,
        val email: String,
        val phone: String,
        val updated: LocalDateTime = LocalDateTime.now(),
        val created: LocalDateTime = LocalDateTime.now())

fun Provider.isNew(): Boolean = id == null


