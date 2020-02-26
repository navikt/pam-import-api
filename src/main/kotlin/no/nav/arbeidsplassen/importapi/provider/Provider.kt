package no.nav.arbeidsplassen.importapi.provider

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Provider(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val identifier: String,
        val email: String,
        val phone: String,
        val updated: LocalDateTime = LocalDateTime.now(),
        val created: LocalDateTime = LocalDateTime.now())

fun Provider.isNew(): Boolean = id == null


