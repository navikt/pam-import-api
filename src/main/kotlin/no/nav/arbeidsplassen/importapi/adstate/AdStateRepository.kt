package no.nav.arbeidsplassen.importapi.adstate

import java.sql.PreparedStatement
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.CrudRepository
import no.nav.arbeidsplassen.importapi.repository.QueryLog.QUERY_LOG
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

interface AdStateRepository : CrudRepository<AdState> {
    fun findByProviderIdAndReference(providerId: Long, reference: String): AdState?
    fun findByUuid(uuid: String): AdState?
    fun findByUuidAndProviderId(uuid: String, providerId: Long): AdState?
    fun deleteByProviderId(providerId: Long)
}

class JdbcAdStateRepository(private val txTemplate: TxTemplate) : AdStateRepository,
    BaseCrudRepository<AdState>(txTemplate) {

    override val insertSQL =
        """insert into ad_state (uuid, reference, provider_id, json_payload, version_id, created) values (?,?,?,?,?,?)"""
    override val updateSQL =
        """update ad_state set uuid=?, reference=?, provider_id=?, json_payload=?, version_id=?, created=?, updated=current_timestamp where id=?"""
    override val deleteSQL = """delete from ad_state where id = ?"""
    override val findSQL: String =
        """select id, uuid, provider_id, reference, version_id, json_payload, created, updated from ad_state where id=?"""
    override val findAllSQL: String =
        """select id, uuid, provider_id, reference, version_id, json_payload, created, updated from ad_state"""

    val findByProviderIdAndReferenceSQL =
        """select id, uuid, provider_id, reference, version_id, json_payload, created, updated from ad_state where provider_id=? and reference=?"""
    val findByUuidSQL =
        """select id, uuid, provider_id, reference, version_id, json_payload, created, updated from ad_state where uuid=?"""
    val findByUuidAndProviderIdSQL =
        """select id, uuid, provider_id, reference, version_id, json_payload, created, updated from ad_state where uuid=? and provider_id=?"""
    val deleteByProviderIdSQL =
        """delete from ad_state where provider_id = ?"""

    override fun ResultSet.mapToEntity(): AdState = AdState(
        id = getLong("id"),
        uuid = getString("uuid"),
        providerId = getLong("provider_id"),
        reference = getString("reference"),
        versionId = getLong("version_id"),
        jsonPayload = getString("json_payload"),
        created = getTimestamp("created").toLocalDateTime(),
        updated = getTimestamp("updated").toLocalDateTime()
    )

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: AdState) {
        setString(1, entity.uuid)
        setString(2, entity.reference)
        setLong(3, entity.providerId)
        setString(4, entity.jsonPayload)
        setLong(5, entity.versionId)
        setTimestamp(6, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        } else {
            setLong(7, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    override fun AdState.kopiMedNyId(nyId: Long): AdState =
        this.copy(id = nyId)

    override fun findByProviderIdAndReference(providerId: Long, reference: String): AdState? =
        singleFind(findByProviderIdAndReferenceSQL) { p ->
            p.setLong(1, providerId)
            p.setString(2, reference)
        }

    override fun findByUuid(uuid: String): AdState? =
        singleFind(findByUuidSQL) { p ->
            p.setObject(1, uuid)
        }

    override fun findByUuidAndProviderId(uuid: String, providerId: Long): AdState? =
        singleFind(findByUuidAndProviderIdSQL) { p ->
            p.setObject(1, uuid)
            p.setLong(2, providerId)
        }

    override fun deleteByProviderId(providerId: Long) {
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteByProviderIdSQL).apply {
                setLong(1, providerId)
                executeUpdate()
            }
        }
    }
}
