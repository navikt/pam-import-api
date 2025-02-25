package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.*
import jakarta.transaction.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.config.TxTemplate
import no.nav.arbeidsplassen.importapi.provider.Provider

abstract class AdminStatusRepository(private val txTemplate: TxTemplate) {

    val insertSQL = """insert into "admin_status" ("uuid", "status", "message", "reference", "provider_id", "version_id", "created", "publish_status") values(?,?,?,?,?,?,?,?)"""
    val updateSQL = """update "admin_status" set "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "version_id"=?, "created"=?, "publish_status"=?, "updated"=current_timestamp where "id"=?"""
    val findByUuidSQL = """jfdkjdk"""
    val findByVersionIdAndProviderIdSQL = """jfdkjdk"""
    val findByProviderIdAndReferenceSQL = """jfdkjdk"""
    val findByVersionIdSQL = """jfdkjdk"""

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

    fun saveAll(entities: Iterable<AdminStatus>): List<AdminStatus> {
        return entities.map { save(it) }.toList()
    }

    fun findByProviderIdAndReference(providerId: Long, reference: String): AdminStatus? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByUuidSQL)
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

    fun findByVersionId(versionId: Long): List<AdminStatus> {

    }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): List<AdminStatus> {

    }

    fun findByUuid(uuid:String): AdminStatus? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByUuidSQL)
                .apply {
                    setObject(1, uuid)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapAdminStatus()
                    }
                    return@doInTransactionNullable null
                }
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
}
