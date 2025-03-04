package no.nav.arbeidsplassen.importapi.adadminstatus

import jakarta.inject.Singleton
import java.sql.PreparedStatement
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

@Singleton
class AdminStatusRepository(private val txTemplate: TxTemplate) : BaseCrudRepository<AdminStatus>(txTemplate) {

    override val insertSQL =
        """insert into "admin_status" ("uuid", "status", "message", "reference", "provider_id", "version_id", "created", "publish_status") values(?,?,?,?,?,?,?,?)"""
    override val updateSQL =
        """update "admin_status" set "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "version_id"=?, "created"=?, "publish_status"=?, "updated"=current_timestamp where "id"=?"""
    override val findSQL = """select * from "admin_status" where id = ?"""
    override val findAllSQL = """select * from "admin_status""""
    override val deleteSQL = """delete from "admin_status" where id = ?"""

    val findByUuidSQL = """select * from "admin_status" where uuid = ?"""
    val findByVersionIdAndProviderIdSQL = """select * from "admin_status" where version_id = ? and provider_id = ?"""
    val findByProviderIdAndReferenceSQL = """select * from "admin_status" where provider_id = ? and reference = ?"""

    fun findByProviderIdAndReference(providerId: Long, reference: String): AdminStatus? =
        singleFind(findByProviderIdAndReferenceSQL) {
            it.setLong(1, providerId)
            it.setString(2, reference)
        }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): List<AdminStatus> =
        listFind(findByVersionIdAndProviderIdSQL) {
            it.setLong(1, versionId)
            it.setLong(2, providerId)
        }

    fun findByUuid(uuid: String): AdminStatus? =
        singleFind(findByUuidSQL) {
            it.setObject(1, uuid)
        }

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: AdminStatus) {
        var parIndex = 0
        setString(++parIndex, entity.uuid)
        setString(++parIndex, entity.status.name)
        setString(++parIndex, entity.message)
        setString(++parIndex, entity.reference)
        setLong(++parIndex, entity.providerId)
        setLong(++parIndex, entity.versionId)
        setTimestamp(++parIndex, entity.created.toTimeStamp())
        setString(++parIndex, entity.publishStatus.name)
        if (!entity.isNew()) {
            setLong(++parIndex, entity.id!!)
        }
    }

    override fun ResultSet.mapToEntity() = AdminStatus(
        id = getLong("id"),
        uuid = getString("uuid"),
        status = Status.valueOf(getString("status")),
        message = getString("message"),
        reference = getString("reference"),
        providerId = getLong("provider_id"),
        versionId = getLong("version_id"),
        created = getTimestamp("created").toLocalDateTime(),
        updated = getTimestamp("updated").toLocalDateTime(),
        publishStatus = PublishStatus.valueOf(getString("publish_status")),
    )

    override fun AdminStatus.kopiMedNyId(nyId: Long): AdminStatus =
        this.copy(id = nyId)
}
