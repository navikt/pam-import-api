package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import jakarta.inject.Singleton
import java.sql.PreparedStatement
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

@Singleton
class AdStateRepository(private val txTemplate: TxTemplate) : BaseCrudRepository<AdState>(txTemplate) {

    override val insertSQL =
        """insert into "ad_state" ("uuid", "reference", "provider_id", "json_payload", "version_id", "created") values (?,?,?,?,?,?)"""
    override val updateSQL =
        """update "ad_state" set "uuid"=?,"reference"=?, "provider_id"=?, "json_payload"=?, "version_id"=?, "created"=?, "updated"=current_timestamp where "id"=?"""
    override val deleteSQL = """delete from "ad_state" where id = ?"""
    override val findSQL: String = """select * from "ad_state" where "id"=?"""
    override val findAllSQL: String = """select * from "ad_state""""

    val findByProviderIdAndReferenceSQL = """select * from "ad_state" where "provider_id"=? and "reference"=?"""
    val findByUuidSQL = """select * from "ad_state" where "uuid"=?"""
    val findByUuidAndProviderIdSQL = """select * from "ad_state" where "uuid"=? and "provider_id"=?"""

    fun findByProviderIdAndReference(providerId: Long, reference: String): AdState? =
        singleFind(findByProviderIdAndReferenceSQL) { p ->
            p.setLong(1, providerId)
            p.setString(2, reference)
        }

    // fun list(pageable: Pageable): Slice<AdState>

    fun findByUuid(uuid: String): AdState? =
        singleFind(findByUuidSQL) { p ->
            p.setObject(1, uuid)
        }

    fun findByUuidAndProviderId(uuid: String, providerId: Long): AdState? =
        singleFind(findByUuidAndProviderIdSQL) { p ->
            p.setObject(1, uuid)
            p.setLong(2, providerId)
        }

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

    override fun AdState.kopiMedNyId(nyId: Long): AdState =
        this.copy(id = nyId)
}
