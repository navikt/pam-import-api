package no.nav.arbeidsplassen.importapi.dao

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
data class AdState(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val uuid: UUID = UUID.randomUUID(),
        val providerId: Long,
        val reference: String,
        val transferVersion: Long,
        val jsonPayload: String,
        val created: LocalDateTime = LocalDateTime.now(),
        val updated: LocalDateTime = LocalDateTime.now()
)

fun AdState.isNew(): Boolean = id == null

