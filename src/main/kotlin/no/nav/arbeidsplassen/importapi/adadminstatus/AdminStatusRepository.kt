package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.*
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class AdminStatusRepository(private val connection: Connection): CrudRepository<AdminStatus, Long> {

    val insertSQL = """insert into "admin_status" ("uuid", "status", "message", "reference", "provider_id", "version_id", "created", "publish_status") values(?,?,?,?,?,?,?,?)"""
    val updateSQL = """update "admin_status" set "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "version_id"=?, "created"=?, "publish_status"=?, "updated"=current_timestamp where "id"=?"""

    @Transactional
    override fun <S : AdminStatus> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
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
        var parIndex=1
        setString(parIndex++, entity.uuid)
        setString(parIndex++, entity.status.name)
        setString(parIndex++, entity.message)
        setString(parIndex++, entity.reference)
        setLong(parIndex++, entity.providerId)
        setLong(parIndex++, entity.versionId)
        setTimestamp(parIndex++, entity.created.toTimeStamp())
        setString(parIndex++, entity.publishStatus.name)
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(parIndex++, entity.id!!)
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
