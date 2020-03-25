package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import javax.transaction.Transactional


@JdbcRepository(dialect = Dialect.ORACLE)
abstract class ProviderRepository(val connection:Connection): CrudRepository<Provider, Long> {

    val insertSQL = """INSERT INTO "PROVIDER" ("JWTID", "IDENTIFIER", "EMAIL", "PHONE", "CREATED") VALUES (?,?,?,?,?)"""
    val updateSQL = """UPDATE "PROVIDER" SET "JWTID"=?, "IDENTIFIER"=?, "EMAIL"=?, "PHONE"=?, "CREATED"=? WHERE "ID"=?"""

    @Transactional
    override fun <S : Provider> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, arrayOf("ID")).apply {
                prepareSQL(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id = generatedKeys.getLong(1)) as S
            }
        }
        else {
            connection.prepareStatement(updateSQL).apply {
                prepareSQL(entity)
                check(executeUpdate() == 1 )
                return entity
            }
        }
    }

    @Transactional
    override fun <S : Provider> saveAll(entities: Iterable<S>): Iterable<S> {
        return entities.map { save(it) }.toList()
    }

    @Transactional
    abstract fun list(pageable: Pageable): Slice<Provider>

    private fun PreparedStatement.prepareSQL(entity: Provider) {
        setString(1, entity.jwtid)
        setString(2, entity.identifier)
        setString(3, entity.email)
        setString(4, entity.phone)
        setObject(5, entity.created)
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(6, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")

        }
    }
}

