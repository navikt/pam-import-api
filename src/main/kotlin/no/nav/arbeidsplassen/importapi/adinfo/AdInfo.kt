package no.nav.arbeidsplassen.importapi.adinfo

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime

@MappedEntity
data class AdInfo(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val providerId: Long,
    val uuid: String,
    val reference: String,
    @field:TypeDef(type = DataType.JSON)
    val activity: MutableMap<String,Any> = mutableMapOf(),
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
)

fun AdInfo.isNew(): Boolean = id == null
