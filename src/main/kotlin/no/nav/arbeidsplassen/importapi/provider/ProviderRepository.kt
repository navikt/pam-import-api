package no.nav.arbeidsplassen.importapi.provider

import java.sql.PreparedStatement
import java.sql.ResultSet
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.CrudRepository
import no.nav.arbeidsplassen.importapi.repository.QueryLog.QUERY_LOG
import no.nav.arbeidsplassen.importapi.repository.TxTemplate

interface ProviderRepository : CrudRepository<Provider> {
    fun saveOnMigrate(entities: Iterable<Provider>) // Er ikke i bruk?
}

class JdbcProviderRepository(private val txTemplate: TxTemplate) : ProviderRepository,
    BaseCrudRepository<Provider>(txTemplate) {

    override val insertSQL =
        """insert into provider (jwtid, identifier, email, phone, created) values (?,?,?,?,?)"""
    override val updateSQL =
        """update provider set jwtid=?, identifier=?, email=?, phone=?, created=?, updated=current_timestamp where id=?"""
    override val findSQL =
        """select id, identifier, jwtid, email, phone, created, updated from provider where id = ?"""
    override val findAllSQL =
        """select id, identifier, jwtid, email, phone, created, updated from provider"""
    override val deleteSQL = """delete from provider where id = ?"""
    val migrateSQL =
        """insert into provider (jwtid, identifier, email, phone, created, updated, id) values (?,?,?,?,?,?,?)"""

    override fun ResultSet.mapToEntity() = Provider(
        id = this.getLong("id"),
        identifier = this.getString("identifier"),
        jwtid = this.getString("jwtid"),
        email = this.getString("email"),
        phone = this.getString("phone"),
        created = this.getTimestamp("created").toLocalDateTime(),
        updated = this.getTimestamp("updated").toLocalDateTime()
    )

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: Provider) {
        setString(1, entity.jwtid)
        setString(2, entity.identifier)
        setString(3, entity.email)
        setString(4, entity.phone)
        setTimestamp(5, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        } else {
            setLong(6, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    override fun Provider.kopiMedNyId(nyId: Long): Provider =
        this.copy(id = nyId)

    override fun saveOnMigrate(entities: Iterable<Provider>) {
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
}
