package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.LocalDateTime
import java.util.*
import jakarta.transaction.Transactional
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.isNew
import no.nav.arbeidsplassen.importapi.config.TxTemplate

@Singleton
 class AdStateRepository(private val txTemplate: TxTemplate){

    val insertSQL = """insert into "ad_state" ("uuid", "reference", "provider_id", "json_payload", "version_id", "created") values (?,?,?,?,?,?)"""
    val updateSQL = """update "ad_state" set "uuid"=?,"reference"=?, "provider_id"=?, "json_payload"=?, "version_id"=?, "created"=?, "updated"=current_timestamp where "id"=?"""
    val findByProviderIdAndReferenceSQL = ""
    val deleteSQL = """delete from "ad_state" where id = ?"""


    fun save(entity: AdState): AdState {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            if (entity.isNew()) {
                connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                    prepareSQL(entity)
                    execute()
                    check(generatedKeys.next())
                }.use { entity.copy(id = it.generatedKeys.getLong(1)) }
            } else {
                connection.prepareStatement(updateSQL).apply {
                    prepareSQL(entity)
                    check(executeUpdate() == 1)
                }
                entity
            }
        }
    }

    fun saveAll(entities: Iterable<AdState>): List<AdState> {
        return entities.map { save(it) }.toList()
    }

    fun findByProviderIdAndReference(providerId: Long, reference: String): AdState? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findByProviderIdAndReferenceSQL)
                .apply {
                    setLong(1, providerId)
                    setString(2, reference)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapToEntity()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    // fun list(pageable: Pageable): Slice<AdState>


    fun findByUuid(uuid: String): AdState? {
        return TODO()
    }

    fun findByUuidAndProviderId(uuid: String, providerId: Long): Optional<AdState> {
        return TODO()
    }

    fun findById(id: Long): AdState? {
        TODO()
    }

    fun findAll(): List<AdState> {
        TODO()
    }

    fun deleteById(id: Long) {
        txTemplate.doInTransaction{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteSQL).apply {
                setObject(1, id)
                check(executeUpdate() == 1)
                // HPH: Skal vi feile hvis vi prøver å slette noe som ikke finnes? Jeg tror det, hvis jeg tolker dette riktig:
                // https://micronaut-projects.github.io/micronaut-data/2.4.1/api/io/micronaut/data/repository/CrudRepository.html#deleteById-ID-
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

    private fun ResultSet.mapToEntity(): AdState {
        TODO()
    }

    private fun ResultSet.mapToList() : List<AdState> =
        use {
            generateSequence { if (it.next()) it.mapToEntity() else null }.toList()
        }
}
