package no.nav.arbeidsplassen.importapi.adinfo

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

@MappedEntity
data class AdInfo(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    val activity: MutableMap<String,Any> = mutableMapOf(),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun AdInfo.isNew(): Boolean = id == null
