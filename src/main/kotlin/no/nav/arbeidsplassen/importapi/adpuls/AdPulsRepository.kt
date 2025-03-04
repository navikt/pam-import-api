package no.nav.arbeidsplassen.importapi.adpuls

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.runtime.config.DataSettings
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

class AdPulsRepository(private val txTemplate: TxTemplate, private val objectMapper: ObjectMapper) :
    BaseCrudRepository<AdPuls>(txTemplate) {

    override val insertSQL =
        """insert into "ad_puls" ("provider_id", "uuid", "reference", "type", "total", "created", "updated" ) values (?,?,?,?,?,?, current_timestamp)"""
    override val updateSQL =
        """update "ad_puls" set "provider_id"=?, "uuid"=?,"reference"=?, "type"=?, "total"=?, "created"=?, "updated"=current_timestamp where "id"=?"""
    override val findSQL = """select * from "ad_puls" where id = ?"""
    override val findAllSQL = """select * from "ad_puls""""
    override val deleteSQL: String = """delete from "ad_puls" where id = ?"""

    val findByUuidAndTypeSQL = """select * from "ad_puls" where uuid = ? and type = ?"""
    val findByUuid = """select * from "ad_puls" where uuid = ?"""
    val findByProviderIdAndReferenceSQL = """select * from "ad_puls" where provider_id = ? and reference = ?"""
    val val deleteByUpdatedBeforeSQL = """delete from "ad_puls" where updated < ?"""

    fun findByUuidAndType(uuid: String, type: PulsEventType): AdPuls =
        singleFind(findByUuidAndTypeSQL) {
            it.setString(1, uuid)
            it.setString(2, type.name)
        }!! // TODO Hvilken exception bør hives?

    fun findByUuid(uuid: String): List<AdPuls> =
        listFind(findByUuid) {
            it.setString(1, uuid)
        }

    fun findByProviderIdAndReference(providerId: Long, reference: String): List<AdPuls> =
        listFind((findByProviderIdAndReferenceSQL)) {
            it.setLong(1, providerId)
            it.setString(2, reference)
        }

    fun findByProviderIdAndUpdatedAfter(
        providerId: Long,
        updated: LocalDateTime,
        pageable: Pageable
    ): Slice<AdPuls> {
        TODO()
    }

    fun deleteByUpdatedBefore(before: LocalDateTime): Long =
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteByUpdatedBeforeSQL).apply {
                setTimestamp(1, before.toTimeStamp())
            }.executeUpdate().toLong()
        }

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: AdPuls) {
        var index = 0
        setLong(++index, entity.providerId)
        setString(++index, entity.uuid)
        setString(++index, entity.reference)
        setString(++index, entity.type.name)
        setLong(++index, entity.total)
        setTimestamp(++index, entity.created.toTimeStamp())
        if (entity.isNew()) {
            DataSettings.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        } else {
            setLong(++index, entity.id!!)
            DataSettings.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    override fun ResultSet.mapToEntity(): AdPuls = AdPuls(
        id = getLong("id"),
        providerId = getLong("provider_id"),
        uuid = getString("uuid"),
        reference = getString("reference"),
        type = PulsEventType.valueOf(getString("type")),
        total = getLong("total"),
        created = getTimestamp("created").toLocalDateTime(),
        updated = getTimestamp("updated").toLocalDateTime()
    )

    override fun AdPuls.kopiMedNyId(nyId: Long): AdPuls =
        this.copy(id = nyId)
}
