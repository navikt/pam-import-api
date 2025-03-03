package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.config.TxTemplate

abstract class CRUDRepositoryHelper<T : Entity>(private val txTemplate: TxTemplate) : CRUDRepository<T> {

    fun saveOrUpdate(entity: T): T {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            if (entity.isNew()) {
                connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                    prepareSQLSaveOrUpdate(entity)
                    execute()
                    check(generatedKeys.next())
                }.use {
                    @Suppress("UNCHECKED_CAST")
                    entity.kopiMedNyId(it.generatedKeys.getLong(1))
                }
            } else {
                connection.prepareStatement(updateSQL).apply {
                    prepareSQLSaveOrUpdate(entity)
                    check(executeUpdate() == 1)
                }
                entity
            }
        }
    }

    fun saveAll(entities: Iterable<T>): List<T> {
        return entities.map { saveOrUpdate(it) }.toList()
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

    fun findById(id: Long): T? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findSQL)
                .apply {
                    setObject(1, id)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapToEntity()
                    }
                    return@doInTransactionNullable null
                }
        }
    }
    fun findAll(): List<T> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection
                .prepareStatement(findAllSQL).executeQuery()
                .use { generateSequence { if (it.next()) it.mapToEntity() else null }.toList() }
        }
    }

    protected fun singleFind(sql : String, applyFunctions : (PreparedStatement) -> PreparedStatement): T? {
        return txTemplate.doInTransactionNullable{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(sql)
                .apply {
                    applyFunctions.invoke(this)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if( rs.next()) {
                        return@doInTransactionNullable rs.mapToEntity()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    protected fun listFind(sql: String, applyFunctions : (PreparedStatement) -> PreparedStatement): List<T> {
        return txTemplate.doInTransaction{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(sql)
                .apply {
                    applyFunctions(this)
                }
                .executeQuery()
                .mapToList()
        }
    }

    private fun ResultSet.mapToList() : List<T> =
        use {
            generateSequence { if (it.next()) it.mapToEntity() else null }.toList()
        }

    fun LocalDateTime.toTimeStamp(): Timestamp {
        return Timestamp.valueOf(this)
    }
}

interface Entity {
    var id: Long?
    fun isNew() : Boolean = id == null
}

interface CRUDRepository<T: Entity> {
    fun PreparedStatement.prepareSQLSaveOrUpdate(entity: T)
    fun ResultSet.mapToEntity(): T
    fun T.kopiMedNyId(id: Long): T
    val insertSQL : String
    val updateSQL: String
    val deleteSQL: String
    val findSQL: String
    val findAllSQL: String
}
