package no.nav.arbeidsplassen.importapi.adstate

import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.*

@Entity
data class AdState(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val uuid: String = UUID.randomUUID().toString(),
        val providerId: Long,
        val reference: String,
        val versionId: Long,
        val jsonPayload: String,
        val created: LocalDateTime = LocalDateTime.now(),
        val updated: LocalDateTime = LocalDateTime.now()
)

fun AdState.isNew(): Boolean = id == null

