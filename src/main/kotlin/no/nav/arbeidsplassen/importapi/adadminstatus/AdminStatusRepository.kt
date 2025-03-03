package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.config.TxTemplate

@Singleton
class AdminStatusRepository(private val txTemplate: TxTemplate) {

    val insertSQL = """insert into "admin_status" ("uuid", "status", "message", "reference", "provider_id", "version_id", "created", "publish_status") values(?,?,?,?,?,?,?,?)"""
    val updateSQL = """update "admin_status" set "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "version_id"=?, "created"=?, "publish_status"=?, "updated"=current_timestamp where "id"=?"""
    val findByUuidSQL = """select * from "admin_status" where uuid = ?"""
    val findByIdSQL = """select * from "admin_status" where id = ?"""
    val findAllSQL = """select * from "admin_status""""
    val findByVersionIdAndProviderIdSQL = """select * from "admin_status" where version_id = ? and provider_id = ?"""
    val findByProviderIdAndReferenceSQL = """select * from "admin_status" where provider_id = ? and reference = ?"""
    val deleteSQL = """delete from "admin_status" where id = ?"""


    fun save(entity: AdminStatus): AdminStatus {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            if (entity.isNew()) {
                connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                    prepareSQL(entity)
                    execute()
                    check(generatedKeys.next())
                }.use { entity.copy(id = it.generatedKeys.getLong(1)) }
            } else {
                connection.prepareStatement(updateSQL).apply {
                    prepareSQL(entity)
                    check(executeUpdate() == 1)
                }
                entity
            }
        }
    }

    fun saveAll(entities: Iterable<AdminStatus>): List<AdminStatus> {
        return entities.map { save(it) }.toList()
    }

    fun findByProviderIdAndReference(providerId: Long, reference: String): AdminStatus? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByProviderIdAndReferenceSQL)
                .apply {
                    setLong(1, providerId)
                    setString(2, reference)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapAdminStatus()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): List<AdminStatus> {
        return txTemplate.doInTransaction{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByVersionIdAndProviderIdSQL)
                .apply {
                    setLong(1, versionId)
                    setLong(2, providerId)
                }
                .executeQuery()
                .mapToList()
        }
    }

    fun findByUuid(uuid:String): AdminStatus? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByUuidSQL)
                .apply {
                    setObject(1, uuid)
                }
                .use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapAdminStatus()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    fun deleteById(id: Long) {
        txTemplate.doInTransaction{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteSQL).apply {
                setObject(1, id)
                check(executeUpdate() == 1)
                // HPH: Skal vi feile hvis vi prøver å slette noe som ikke finnes? Jeg tror det, hvis jeg tolker dette riktig:
                // https://micronaut-projects.github.io/micronaut-data/2.4.1/api/io/micronaut/data/repository/CrudRepository.html#deleteById-ID-
            }
        }
    }

    fun findById(id: Long): AdminStatus? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByIdSQL)
                .apply {
                    setLong(1, id)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapAdminStatus()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    fun findAll(): List<AdminStatus> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection
                .prepareStatement(findAllSQL).executeQuery()
                .use { generateSequence { if (it.next()) it.mapAdminStatus() else null }.toList() }
        }
    }

    private fun PreparedStatement.prepareSQL(entity: AdminStatus) {
        var parIndex=0
        setString(++parIndex, entity.uuid)
        setString(++parIndex, entity.status.name)
        setString(++parIndex, entity.message)
        setString(++parIndex, entity.reference)
        setLong(++parIndex, entity.providerId)
        setLong(++parIndex, entity.versionId)
        setTimestamp(++parIndex, entity.created.toTimeStamp())
        setString(++parIndex, entity.publishStatus.name)
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(++parIndex, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    private fun ResultSet.mapAdminStatus() = AdminStatus(
        id = getLong("id"),
        uuid = getString("uuid"),
        status = Status.valueOf(getString("status")),
        message = getString("message"),
        reference = getString("reference"),
        providerId = getLong("providerId"),
        versionId = getLong("versionId"),
        created = getTimestamp("created").toLocalDateTime(),
        updated = getTimestamp("updated").toLocalDateTime(),
        publishStatus = PublishStatus.valueOf(getString("publishStatus")),
    )

    private fun ResultSet.mapToList() : List<AdminStatus> =
        use {
            generateSequence { if (it.next()) it.mapAdminStatus() else null }.toList()
        }
}
