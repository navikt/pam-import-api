package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.LocalDateTime
import java.util.*
import jakarta.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class AdStateRepository(val connection: Connection): CrudRepository<AdState, Long> {

    val insertSQL = """insert into "ad_state" ("uuid", "reference", "provider_id", "json_payload", "version_id", "created") values (?,?,?,?,?,?)"""
    val updateSQL = """update "ad_state" set "uuid"=?,"reference"=?, "provider_id"=?, "json_payload"=?, "version_id"=?, "created"=?, "updated"=current_timestamp where "id"=?"""

    @Transactional
    override fun <S : AdState> save(entity: S): S {
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

    private fun PreparedStatement.prepareSQL(entity: AdState) {
        setString(1, entity.uuid)
        setString(2, entity.reference)
        setLong(3, entity.providerId)
        setString(4, entity.jsonPayload)
        setLong(5, entity.versionId)
        setTimestamp(6, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(7, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")

        }
    }

    @Transactional
    override fun <S : AdState> saveAll(entities: Iterable<S>): List<S> {
        return entities.map { save(it) }.toList()
    }

    @Transactional
    abstract fun findByProviderIdAndReference(providerId: Long, reference: String): Optional<AdState>

    @Transactional
    abstract fun list(pageable: Pageable): Slice<AdState>

    @Transactional
    abstract fun findByUpdatedGreaterThanEquals(updated: LocalDateTime, pageable: Pageable): Slice<AdState>

    @Transactional
    abstract fun findByUuid(uuid: String): AdState?


    @Transactional
    abstract fun findByUuidAndProviderId(uuid: String, providerId: Long): Optional<AdState>

    @Transactional
    abstract fun list(versionId: Long, pageable: Pageable): Slice<AdState>

    @Transactional
    abstract fun list(versionId: Long, providerId: Long, pageable: Pageable): Slice<AdState>

}
