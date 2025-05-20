package no.nav.arbeidsplassen.importapi.transferlog


import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.BaseCrudRepository
import no.nav.arbeidsplassen.importapi.repository.CrudRepository
import no.nav.arbeidsplassen.importapi.repository.QueryLog.QUERY_LOG
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.slf4j.LoggerFactory

interface TransferLogRepository : CrudRepository<TransferLog> {
    fun existsByProviderIdAndMd5(providerId: Long, md5: String): Boolean

    // TODO: Gir denne mening? Id er pk, så hvorfor har vi med providerId? For å sikre at en provider ikke får tilgang til andres data?
    fun findByIdAndProviderId(id: Long, providerId: Long): TransferLog?
    fun findByStatus(status: TransferLogStatus): List<TransferLog>
    fun deleteByUpdatedBefore(updated: LocalDateTime)
}

class JdbcTransferLogRepository(private val txTemplate: TxTemplate) : TransferLogRepository,
    BaseCrudRepository<TransferLog>(txTemplate) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogRepository::class.java)
    }

    override val insertSQL =
        """insert into transfer_log (provider_id, items, md5, payload, status, message, created) values (?,?,?,?,?,?,?)"""
    override val updateSQL =
        """update transfer_log set provider_id=?, items=?, md5=?, payload=?, status=?, message=?, created=?, updated=current_timestamp where id=?"""
    override val findSQL: String =
        """select id, provider_id, md5, items, payload, status, message, created, updated from transfer_log where id = ?"""
    override val findAllSQL: String =
        """select id, provider_id, md5, items, payload, status, message, created, updated from transfer_log"""
    override val deleteSQL: String = """delete from transfer_log where id = ?"""

    val findByIdAndProviderIdSQL =
        """select id, provider_id, md5, items, payload, status, message, created, updated from transfer_log where id = ? and provider_id = ?"""
    val findByStatusPageableAsc =
        """select id, provider_id, md5, items, payload, status, message, created, updated from transfer_log where status = ? order by updated asc offset 0 LIMIT 100"""
    val findByProviderIdAndMd5SQL =
        """select id, provider_id, md5, items, payload, status, message, created, updated from transfer_log where provider_id = ? and md5 = ?"""
    val deleteByUpdatedBeforeSQL = """delete from transfer_log where updated < ?"""

    override fun ResultSet.mapToEntity(): TransferLog = TransferLog(
        id = getLong("id"),
        providerId = getLong("provider_id"),
        md5 = getString("md5"),
        items = getInt("items"),
        payload = getString("payload"),
        status = TransferLogStatus.valueOf(getString("status")),
        message = getString("message"),
        created = getTimestamp("created").toLocalDateTime(),
        updated = getTimestamp("updated").toLocalDateTime()
    )

    override fun PreparedStatement.prepareSQLSaveOrUpdate(entity: TransferLog) {
        setObject(1, entity.providerId)
        setInt(2, entity.items)
        setString(3, entity.md5)
        setString(4, entity.payload)
        setString(5, entity.status.name)
        setString(6, entity.message)
        setTimestamp(7, entity.created.toTimeStamp())
        if (entity.isNew()) {
            QUERY_LOG.debug("Executing SQL INSERT: $insertSQL")
        } else {
            setLong(8, entity.id!!)
            QUERY_LOG.debug("Executing SQL UPDATE: $updateSQL")
        }
    }

    override fun TransferLog.kopiMedNyId(nyId: Long): TransferLog =
        this.copy(id = nyId)

    override fun existsByProviderIdAndMd5(providerId: Long, md5: String): Boolean =
        singleFind(findByProviderIdAndMd5SQL) {
            it.setLong(1, providerId)
            it.setString(2, md5)
        } != null

    override fun findByIdAndProviderId(id: Long, providerId: Long): TransferLog? =
        singleFind(findByIdAndProviderIdSQL) {
            it.setLong(1, id)
            it.setLong(2, providerId)
        }

    override fun findByStatus(status: TransferLogStatus): List<TransferLog> {
        return listFind(findByStatusPageableAsc) {
            it.setString(1, status.name)
        }
    }

    override fun deleteByUpdatedBefore(updated: LocalDateTime) {
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteByUpdatedBeforeSQL).apply {
                setTimestamp(1, updated.toTimeStamp())
                executeUpdate()
            }
        }
    }

}
