package no.nav.arbeidsplassen.importapi.repository

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

interface Entity {
    var id: Long?
    fun isNew(): Boolean = id == null
}

interface CrudRepository<T : Entity> {
    fun save(entity: T): T
    fun saveAll(entities: Iterable<T>): List<T>
    fun deleteById(id: Long)
    fun findById(id: Long): T?
    fun findAll(): List<T>
}

abstract class BaseCrudRepository<T : Entity>(private val txTemplate: TxTemplate) : CrudRepository<T> {

    companion object {
        private val LOG = LoggerFactory.getLogger(BaseCrudRepository::class.java)
    }

    abstract val insertSQL: String
    abstract val updateSQL: String
    abstract val findSQL: String
    abstract val findAllSQL: String
    abstract val deleteSQL: String

    abstract fun PreparedStatement.prepareSQLSaveOrUpdate(entity: T)
    abstract fun ResultSet.mapToEntity(): T
    abstract fun T.kopiMedNyId(nyId: Long): T

    override fun save(entity: T): T {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            if (entity.isNew()) {
                connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS).apply {
                    prepareSQLSaveOrUpdate(entity)
                    execute()
                    check(generatedKeys.next())
                }.use {
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

    override fun saveAll(entities: Iterable<T>): List<T> {
        return entities.map { save(it) }.toList()
    }

    override fun deleteById(id: Long) {
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteSQL).apply {
                setObject(1, id)
                check(executeUpdate() == 1)
                // HPH: Skal vi feile hvis vi prøver å slette noe som ikke finnes? Jeg tror det, hvis jeg tolker dette riktig:
                // https://micronaut-projects.github.io/micronaut-data/2.4.1/api/io/micronaut/data/repository/CrudRepository.html#deleteById-ID-
            }
        }
    }

    override fun findById(id: Long): T? {
        return txTemplate.doInTransactionNullable { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(findSQL)
                .apply {
                    setObject(1, id)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if (rs.next()) {
                        return@doInTransactionNullable rs.mapToEntity()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    override fun findAll(): List<T> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection
                .prepareStatement(findAllSQL).executeQuery()
                .use { generateSequence { if (it.next()) it.mapToEntity() else null }.toList() }
        }
    }

    protected fun singleFind(sql: String, applyFunctions: (PreparedStatement) -> Unit): T? {
        return txTemplate.doInTransactionNullable { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(sql)
                .apply {
                    applyFunctions.invoke(this)
                }.use {
                    val rs: ResultSet = it.executeQuery()
                    if (rs.next()) {
                        return@doInTransactionNullable rs.mapToEntity()
                    }
                    return@doInTransactionNullable null
                }
        }
    }

    protected fun listFind(sql: String, applyFunctions: (PreparedStatement) -> Unit): List<T> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(sql)
                .apply {
                    applyFunctions(this)
                }
                .executeQuery()
                .mapToList()
        }
    }

    private fun ResultSet.mapToList(): List<T> =
        use { rs: ResultSet ->
            generateSequence { if (rs.next()) rs.mapToEntity() else null }.toList()
        }

    fun LocalDateTime.toTimeStamp(): Timestamp {
        return Timestamp.valueOf(this)
    }
}
