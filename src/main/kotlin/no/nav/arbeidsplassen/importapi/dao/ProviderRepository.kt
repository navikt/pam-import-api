package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.aop.Around
import io.micronaut.core.util.ArgumentUtils
import io.micronaut.data.annotation.Repository
import io.micronaut.data.exceptions.DataAccessException
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.jdbc.operations.AbstractSqlRepositoryOperations
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.data.jdbc.runtime.PreparedStatementCallback
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import javax.inject.Singleton
import javax.transaction.Transactional


@JdbcRepository(dialect = Dialect.ANSI)
abstract class ProviderRepository(val connection:Connection): CrudRepository<Provider, Long> {

    val insert = """INSERT INTO "provider" ("uuid", "username", "email", "created") VALUES (?,?,?,?)"""
    val update = """UPDATE "provider" SET "uuid"=?, "username"=?, "email"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : Provider> save(entity: S): S {
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
                setLong(5, entity.id!!)
                check(executeUpdate() == 1 )
                return entity
            }
        }
    }

    override fun <S : Provider?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    private fun PreparedStatement.setSQLParams(entity: Provider) {
        setObject(1, entity.uuid)
        setString(2, entity.username)
        setString(3, entity.email)
        setObject(4, entity.created)
    }
}


