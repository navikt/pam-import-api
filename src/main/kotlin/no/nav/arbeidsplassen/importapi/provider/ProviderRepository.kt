package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.transaction.Transactional


@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class ProviderRepository(val connection:Connection): CrudRepository<Provider, Long> {

    val insertSQL = """insert into "provider" ("jwtid", "identifier", "email", "phone", "created") values (?,?,?,?,?)"""
    val updateSQL = """update "provider" set "jwtid"=?, "identifier"=?, "email"=?, "phone"=?, "created"=?, "updated"=current_timestamp where "id"=?"""
    val migrateSQL = """insert into "provider" ("jwtid", "identifier", "email", "phone", "created", "updated", "id") values (?,?,?,?,?,?,?)"""

    @Transactional
    override fun <S : Provider> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
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
    fun saveOnMigrate(entities: Iterable<Provider>) {
        entities.forEach {
            connection.prepareStatement(migrateSQL).apply {
                setString(1, it.jwtid)
                setString(2, it.identifier)
                setString(3, it.email)
                setString(4, it.phone)
                setTimestamp(5, it.created.toTimeStamp())
                setTimestamp(6, it.updated.toTimeStamp())
                setObject(7, it.id)
                execute()
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
        setTimestamp(5, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(6, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")

        }
    }
}

fun LocalDateTime.toTimeStamp(): Timestamp {
    return Timestamp.valueOf(this)
}
