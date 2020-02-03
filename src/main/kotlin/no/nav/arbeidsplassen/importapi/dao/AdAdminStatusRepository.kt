package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.transaction.Transactional

@JdbcRepository(dialect = Dialect.ANSI)
abstract class AdAdminStatusRepository(private val connection: Connection): CrudRepository<AdAdminStatus, Long> {

    val insert = """INSERT INTO "ad_admin_status" ("uuid", "status", "message", "reference", "provider_id", "created") VALUES(?,?,?,?,?,?)"""
    val update = """UPDATE "ad_admin_status" SET "uuid"=?, "status"=?, "message"=?, "reference"=?, "provider_id"=?, "created"=? WHERE "id"=?"""


    @Transactional
    override fun <S : AdAdminStatus> save(entity: S): S {
        if (entity.isNew()) {
            connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS).apply {
                setSQLParams(entity)
                execute()
                check(generatedKeys.next())
                @Suppress("UNCHECKED_CAST")
                return entity.copy(id=generatedKeys.getLong("id")) as S
            }
        }
        else {
            connection.prepareStatement(update).apply {
                setSQLParams(entity)
                setLong(7, entity.id!!)
                check (executeUpdate() == 1)
                return entity
            }
        }
    }

    private fun PreparedStatement.setSQLParams(entity: AdAdminStatus) {
        setObject(1, entity.uuid)
        setString(2, entity.status.name)
        setString(3, entity.message)
        setString(4, entity.reference)
        setLong(5, entity.providerId)
        setObject(6, entity.created)
    }

    override fun <S : AdAdminStatus> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}