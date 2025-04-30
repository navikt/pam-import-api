package no.nav.arbeidsplassen.importapi.adpuls

import jakarta.inject.Singleton
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.Pageable
import no.nav.arbeidsplassen.importapi.repository.QueryLog
import no.nav.arbeidsplassen.importapi.repository.Slice
import no.nav.arbeidsplassen.importapi.repository.Sortable
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

@Singleton
class AdPulsRepository(private val txTemplate: TxTemplate) :
    BaseCrudRepository<AdPuls>(txTemplate) {

    override val insertSQL =
        """insert into ad_puls (provider_id, uuid, reference, type, total, created, updated ) values (?,?,?,?,?,?, current_timestamp)"""
    override val updateSQL =
        """update ad_puls set provider_id=?, uuid=?,reference=?, type=?, total=?, created=?, updated=current_timestamp where id=?"""
    override val findSQL =
        """select id, provider_id, uuid, reference, type, total, created, updated from ad_puls where id = ?"""
    override val findAllSQL =
        """select id, provider_id, uuid, reference, type, total, created, updated from ad_puls"""
    override val deleteSQL: String = """delete from ad_puls where id = ?"""

    val findByUuidAndTypeSQL =
        """select id, provider_id, uuid, reference, type, total, created, updated from ad_puls where uuid = ? and type = ?"""
    val deleteByUpdatedBeforeSQL = """delete from ad_puls where updated < ?"""
    val findByProviderIdAndUpdatedAfterAndPageableAscSQL =
        """select id, provider_id, uuid, reference, type, total, created, updated from ad_puls where provider_id = ? and updated > ? order by ? asc offset ? LIMIT ?"""
    val findByProviderIdAndUpdatedAfterAndPageableDescSQL =
        """select id, provider_id, uuid, reference, type, total, created, updated from ad_puls where provider_id = ? and updated > ? order by ? desc offset ? LIMIT ?"""

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

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: AdPuls) {
        var index = 0
        setLong(++index, entity.providerId)
        setString(++index, entity.uuid)
        setString(++index, entity.reference)
        setString(++index, entity.type.name)
        setLong(++index, entity.total)
        setTimestamp(++index, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QueryLog.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        } else {
            setLong(++index, entity.id!!)
            QueryLog.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    override fun AdPuls.kopiMedNyId(nyId: Long): AdPuls =
        this.copy(id = nyId)

    fun findByUuidAndType(uuid: String, type: PulsEventType): AdPuls? =
        singleFind(findByUuidAndTypeSQL) {
            it.setString(1, uuid)
            it.setString(2, type.name)
        }

    fun findByProviderIdAndUpdatedAfter(
        providerId: Long,
        updated: LocalDateTime,
        pageable: Pageable
    ): Slice<AdPuls> {
        val sql = if (pageable.sort.direction == Sortable.Direction.ASC) {
            findByProviderIdAndUpdatedAfterAndPageableAscSQL
        } else {
            findByProviderIdAndUpdatedAfterAndPageableDescSQL
        }
        return listFind(sql) {
            it.setLong(1, providerId)
            it.setTimestamp(2, updated.toTimeStamp())
            it.setString(3, pageable.sort.property.name) // order by
            it.setLong(4, (pageable.offset)) // offset
            it.setInt(5, pageable.size) // limit
        }.let { Slice(it, pageable) }
    }

    fun deleteByUpdatedBefore(before: LocalDateTime): Long =
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteByUpdatedBeforeSQL).apply {
                setTimestamp(1, before.toTimeStamp())
            }.executeUpdate().toLong()
        }
}
