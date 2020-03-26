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

@JdbcRepository(dialect = Dialect.ORACLE)
abstract class AdminStatusRepository(private val connection: Connection): CrudRepository<AdminStatus, Long> {

    val insertSQL = """INSERT INTO "ADMIN_STATUS" ("UUID", "STATUS", "MESSAGE", "REFERENCE", "PROVIDER_ID", "VERSION_ID", "CREATED") VALUES(?,?,?,?,?,?,?)"""
    val updateSQL = """UPDATE "ADMIN_STATUS" SET "UUID"=?, "STATUS"=?, "MESSAGE"=?, "REFERENCE"=?, "PROVIDER_ID"=?, "VERSION_ID"=?, "CREATED"=? WHERE "ID"=?"""

    @Transactional
    override fun <S : AdminStatus> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, arrayOf("ID")).apply {
                prepareSQL(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id=generatedKeys.getLong(1)) as S
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


    private fun PreparedStatement.prepareSQL(entity: AdminStatus) {
        setString(1, entity.uuid)
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
    override fun <S : AdminStatus> saveAll(entities: Iterable<S>): Iterable<S> {
        return entities.map { save(it) }.toList()
    }

    @Transactional
    abstract fun findByProviderIdAndReference(providerId: Long, reference: String): Optional<AdminStatus>

    @Transactional
    abstract fun findByVersionId(versionId: Long): List<AdminStatus>

    @Transactional
    abstract fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): List<AdminStatus>

    @Transactional
    abstract fun findByUuid(uuid:String): Optional<AdminStatus>

}