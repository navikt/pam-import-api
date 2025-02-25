package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.runtime.config.DataSettings.QUERY_LOG
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.config.NavPageable
import no.nav.arbeidsplassen.importapi.config.NavSlice
import no.nav.arbeidsplassen.importapi.config.TxTemplate


interface ProviderRepository {
    fun save(entity: Provider): Provider
    fun saveAll(entities: Iterable<Provider>): List<Provider>
    fun findById(id: Long): Provider?
    fun deleteById(id: Long)
    fun list(page: NavPageable): NavSlice<Provider>
}

class JdbcProviderRepository(val txTemplate: TxTemplate): ProviderRepository {

    val insertSQL = """insert into "provider" ("jwtid", "identifier", "email", "phone", "created") values (?,?,?,?,?)"""
    val updateSQL = """update "provider" set "jwtid"=?, "identifier"=?, "email"=?, "phone"=?, "created"=?, "updated"=current_timestamp where "id"=?"""
    val migrateSQL = """insert into "provider" ("jwtid", "identifier", "email", "phone", "created", "updated", "id") values (?,?,?,?,?,?,?)"""
    val listSQL = """select * from "provider" where "id" > ? order by id asc limit ?"""

    override fun save(entity: Provider): Provider {
        return txTemplate.doInTransaction{ ctx ->
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
                }.use { entity }
            }
        }
    }

    fun saveOnMigrate(entities: Iterable<Provider>) {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            entities.forEach {
                connection.prepareStatement(migrateSQL).apply {
                    setString(1, it.jwtid)
                    setString(2, it.identifier)
                    setString(3, it.email)
                    setString(4, it.phone)
                    setTimestamp(5, it.created.toTimeStamp())
                    setTimestamp(6, it.updated.toTimeStamp())
                    setObject(7, it.id)
                    execute()
                }
            }
        }
    }

    override fun saveAll(entities: Iterable<Provider>): List<Provider> {
        return entities.map { save(it) }.toList()
    }

    override fun findById(id: Long): Provider {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun list(page: NavPageable): NavSlice<Provider> {
        return txTemplate.doInTransaction{ ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(listSQL).apply {
                setObject(1, page.offset)
                setInt(2, page.limit)
            }.use {
                val res: ResultSet = it.executeQuery()
                val providers: MutableList<Provider> = mutableListOf<Provider>()
                while( res.next() ) {
                    providers.add(mapProvider(res))
                }
                return@doInTransaction NavSlice(providers, page)
            }
        }
    }

    fun findByUpdatedGreaterThanEquals(updated: LocalDateTime, pageable: NavPageable): NavSlice<Provider> {
        TODO()
    }
    
    private fun mapProvider(res: ResultSet) = Provider(
        id = res.getLong("id"),
        identifier = res.getString("identifier"),
        jwtid = res.getString("jwtid"),
        email = res.getString("email"),
        phone = res.getString("phone"),
        created = res.getTimestamp("created").toLocalDateTime(),
        updated = res.getTimestamp("updated").toLocalDateTime()
    )

    private fun PreparedStatement.prepareSQL(entity: Provider) {
        setString(1, entity.jwtid)
        setString(2, entity.identifier)
        setString(3, entity.email)
        setString(4, entity.phone)
        setTimestamp(5, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        }
        else {
            setLong(6, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }
}

fun LocalDateTime.toTimeStamp(): Timestamp {
    return Timestamp.valueOf(this)
}
