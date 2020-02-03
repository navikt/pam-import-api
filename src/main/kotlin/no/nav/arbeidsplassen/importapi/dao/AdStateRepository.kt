package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.aop.Around
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class AdStateRepository(val connection: Connection): CrudRepository<AdState, Long> {

    val insert = """INSERT INTO "ad_state" ("uuid", "reference", "provider_id", "json_payload", "version", "created") VALUES (?,?,?,?,?,?)"""
    val update = """UPDATE "ad_state" SET "uuid"=?,"reference"=?, "provider_id"=?, "json_payload"=?, "version"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : AdState> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS).apply {
                setSQLParams(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id = generatedKeys.getLong("id")) as S
            }
        }
        else {
            connection.prepareStatement(update).apply {
                setSQLParams(entity)
                setLong(7, entity.id!!)
                check(executeUpdate() == 1 )
                return entity
            }
        }    }

    override fun <S : AdState?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun PreparedStatement.setSQLParams(entity: AdState) {
        setObject(1, entity.uuid)
        setString(2, entity.reference)
        setLong(3, entity.providerId)
        setString(4, entity.jsonPayload)
        setInt(5, entity.version)
        setObject(6, entity.created)
    }



}