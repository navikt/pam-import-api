package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.jdbc.operations.AbstractSqlRepositoryOperations
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class AdAdminStatusRepository(private val connection: Connection): CrudRepository<AdAdminStatus, Long> {

    val insertSQL = """INSERT INTO "ad_admin_status" ("uuid", "status", "message", "reference", "provider_id", "created") VALUES(?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "ad_admin_status" SET "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : AdAdminStatus> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                prepareSQL(entity, false)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id=generatedKeys.getLong("id")) as S
            }
        }
        else {
            connection.prepareStatement(updateSQL).apply {
                prepareSQL(entity,true)
                check (executeUpdate() == 1)
                return entity
            }
        }
    }


    private fun PreparedStatement.prepareSQL(entity: AdAdminStatus, update: Boolean) {
        setObject(1, entity.uuid)
        setString(2, entity.status.name)
        setString(3, entity.message)
        setString(4, entity.reference)
        setLong(5, entity.providerId)
        setObject(6, entity.created)
        if (update) {
            setLong(7, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
        else {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
    }

    @Transactional
    override fun <S : AdAdminStatus> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        return entities.map { save(it) }.toMutableList()
    }

}