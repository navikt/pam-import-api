package no.nav.arbeidsplassen.importapi.adpuls

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.LocalDateTime
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class AdPulsRepository(private val connection: Connection, private val objectMapper: ObjectMapper):
    CrudRepository<AdPuls, Long> {

    val insertSQL = """insert into "ad_puls" ("provider_id", "uuid", "reference", "type", "total", "created", "updated" ) values (?,?,?,?,?,?, current_timestamp)"""
    val updateSQL = """update "ad_puls" set "provider_id"=?, "uuid"=?,"reference"=?, "type"=?, "total"=?, "created"=?, "updated"=current_timestamp where "id"=?"""

    @Transactional
    override fun <S : AdPuls> save(entity: S): S {

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
    override fun <S : AdPuls> saveAll(entities: Iterable<S>): Iterable<S> {
        return entities.map { save(it) }
    }

    private fun PreparedStatement.prepareSQL(entity: AdPuls) {
        var index=0
        setLong(++index, entity.providerId)
        setString(++index, entity.uuid)
        setString(++index, entity.reference)
        setString(++index, entity.type.name)
        setLong(++index, entity.total)
        setTimestamp(++index, entity.created.toTimeStamp())
        if (entity.isNew()) {
            DataSettings.QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(++index, entity.id!!)
            DataSettings.QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    @Transactional
    abstract fun findByUuidAndType(uuid:String, type: PulsEventType): AdPuls?

    @Transactional
    abstract fun findByUuid(uuid: String): List<AdPuls>

    @Transactional
    abstract fun findByProviderIdAndReference(providerId: Long, reference: String): List<AdPuls>

    @Transactional
    abstract fun findByProviderIdAndUpdatedAfter(providerId: Long, updated: LocalDateTime, pageable: Pageable): Slice<AdPuls>

    @Transactional
    abstract fun deleteByUpdatedBefore(before: LocalDateTime): Long
}
