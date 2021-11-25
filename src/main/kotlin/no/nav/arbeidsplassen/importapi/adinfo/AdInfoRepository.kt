package no.nav.arbeidsplassen.importapi.adinfo

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class AdInfoRepository(private val connection: Connection, private val objectMapper: ObjectMapper):
    CrudRepository<AdInfo, Long> {

    val insertSQL = """insert into "ad_info" ("provider_id", "uuid", "reference", "activity", "created", "updated" ) values (?,?,?,?::jsonb,?, current_timestamp)"""
    val updateSQL = """update "ad_info" set "provider_id"=?, "uuid"=?,"reference"=?, "activity"=?, "created"=?, "updated"=current_timestamp where "id"=?"""

    @Transactional
    override fun <S : AdInfo> save(entity: S): S {

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

    private fun PreparedStatement.prepareSQL(entity: AdInfo) {
        var index=1
        setLong(index, entity.providerId)
        setString(++index, entity.uuid)
        setString(++index, entity.reference)
        setString(++index, objectMapper.writeValueAsString(entity.activity))
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
    abstract fun findByUuid(uuid:String): AdInfo?

}
