package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class AdStateRepository(val connection: Connection): CrudRepository<AdState, Long> {

    val insertSQL = """INSERT INTO "ad_state" ("uuid", "reference", "provider_id", "json_payload", "version", "created") VALUES (?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "ad_state" SET "uuid"=?,"reference"=?, "provider_id"=?, "json_payload"=?, "version"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : AdState> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                prepareSQL(entity, false)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id = generatedKeys.getLong("id")) as S
            }
        }
        else {
            connection.prepareStatement(updateSQL).apply {
                prepareSQL(entity, true)
                check(executeUpdate() == 1 )
                return entity
            }
        }
    }

    @Transactional
    override fun <S : AdState> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        return entities.map { save(it) }.toMutableList()
    }

    private fun PreparedStatement.prepareSQL(entity: AdState, update: Boolean) {
        setObject(1, entity.uuid)
        setString(2, entity.reference)
        setLong(3, entity.providerId)
        setString(4, entity.jsonPayload)
        setInt(5, entity.version)
        setObject(6, entity.created)
        if (update) {
            setLong(7, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
        else {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
    }

}