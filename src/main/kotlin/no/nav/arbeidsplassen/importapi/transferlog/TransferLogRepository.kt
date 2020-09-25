package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.Connection
import java.sql.PreparedStatement
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ORACLE)
abstract class TransferLogRepository(private val connection: Connection): CrudRepository<TransferLog, Long> {

    val insertSQL = """INSERT INTO "TRANSFER_LOG" ("PROVIDER_ID", "ITEMS", "MD5", "PAYLOAD", "STATUS", "MESSAGE", "CREATED") VALUES (?,?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "TRANSFER_LOG" SET "PROVIDER_ID"=?, "ITEMS"=?, "MD5"=?, "PAYLOAD"=?, "STATUS"=?, "MESSAGE"=?, "CREATED"=?, "UPDATED"=CURRENT_TIMESTAMP WHERE "ID"=?"""

    @Transactional
    override fun <S : TransferLog> save(entity: S): S {
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
    abstract fun existsByProviderIdAndMd5(providerId: Long, md5: String): Boolean

    @Transactional
    abstract fun findByIdAndProviderId(id: Long, providerId: Long): Optional<TransferLog>

    @Transactional
    abstract fun findByStatus(status: TransferLogStatus, pageable: Pageable): List<TransferLog>

    @Transactional
    abstract fun deleteByUpdatedBefore(updated: LocalDateTime)

    @Transactional
    abstract fun findByUpdatedGreaterThanEquals(updated: LocalDateTime, pageable: Pageable): Slice<TransferLog>

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
