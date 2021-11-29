package no.nav.arbeidsplassen.importapi.adinfo

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime

@MappedEntity
data class AdPuls(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    @field:TypeDef(type = DataType.STRING)
    val type: PulsEventType,
    val total: Long,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun AdPuls.isNew(): Boolean = id == null
