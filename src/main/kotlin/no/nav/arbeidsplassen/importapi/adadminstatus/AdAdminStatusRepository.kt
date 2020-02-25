package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.*
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class AdAdminStatusRepository(private val connection: Connection): CrudRepository<AdAdminStatus, Long> {

    val insertSQL = """INSERT INTO "ad_admin_status" ("uuid", "status", "message", "reference", "provider_id", "version_id", "created") VALUES(?,?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "ad_admin_status" SET "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "version_id"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : AdAdminStatus> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                prepareSQL(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id=generatedKeys.getLong("id")) as S
            }
        }
        else {
            connection.prepareStatement(updateSQL).apply {
                prepareSQL(entity)
                check (executeUpdate() == 1)
                return entity
            }
        }
    }


    private fun PreparedStatement.prepareSQL(entity: AdAdminStatus) {
        setObject(1, entity.uuid)
        setString(2, entity.status.name)
        setString(3, entity.message)
        setString(4, entity.reference)
        setLong(5, entity.providerId)
        setLong(6, entity.versionId)
        setObject(7, entity.created)
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(8, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")

        }
    }

    @Transactional
    override fun <S : AdAdminStatus> saveAll(entities: Iterable<S>): Iterable<S> {
        return entities.map { save(it) }.toList()
    }

    @Transactional
    abstract fun findByProviderIdAndReference(providerId: Long, reference: String): Optional<AdAdminStatus>

    @Transactional
    abstract fun findByVersionId(versionId: Long): List<AdAdminStatus>

}