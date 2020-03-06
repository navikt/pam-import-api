package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.LocalDateTime
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class TransferLogRepository(private val connection: Connection): CrudRepository<TransferLog, Long> {

    val insertSQL = """INSERT INTO "transfer_log" ("provider_id", "items", "md5", "payload", "status", "message", "created") VALUES (?,?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "transfer_log" SET "provider_id"=?, "items"=?, "md5"=?, "payload"=?, "status"=?, "message"=?, "created"=? WHERE "id"=?"""

    @Transactional
    override fun <S : TransferLog> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                prepareSQL(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id = generatedKeys.getLong("id")) as S
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
    abstract fun existsByProviderIdAndMd5(providerId: Long, md5: String): Boolean

    @Transactional
    abstract fun findByStatus(status: TransferLogStatus, pageable: Pageable): List<TransferLog>

    @Transactional
    abstract fun deleteByUpdatedBefore(updated: LocalDateTime)

    override fun <S : TransferLog> saveAll(entities: Iterable<S>): Iterable<S> {
        return entities.map { save(it) }.toList()
    }

    private fun PreparedStatement.prepareSQL(entity: TransferLog) {
        setObject(1, entity.providerId)
        setInt(2, entity.items)
        setString(3, entity.md5)
        setString(4, entity.payload)
        setString(5, entity.status.name)
        setString(6, entity.message)
        setObject(7, entity.created)
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(8, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }
}