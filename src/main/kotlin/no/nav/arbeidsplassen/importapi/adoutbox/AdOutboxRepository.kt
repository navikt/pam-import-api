package no.nav.arbeidsplassen.importapi.adoutbox

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.TxTemplate


interface AdOutboxRepository {
    fun hentAlle(): List<AdOutbox>
    fun lagre(entity: AdOutbox): Int
    fun hentUprosesserteMeldinger(batchSize: Int = 1000, outboxDelay: Long = 30): List<AdOutbox>
    fun lagreFlere(entities: Iterable<AdOutbox>) = entities.sumOf { lagre(it) }
    fun markerSomProsessert(adOutbox: AdOutbox): Boolean
    fun markerSomFeilet(adOutbox: AdOutbox): Boolean

    // SKAL BARE BRUKES I TESTER
    fun slettAlle()
}

class JdbcAdOutboxRepository(private val txTemplate: TxTemplate) : AdOutboxRepository {

    val lagreSQL = """
            INSERT INTO ad_outbox (uuid, payload, opprettet_dato, har_feilet, antall_forsok, siste_forsok_dato, prosessert_dato)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

    val selectSQL =
        "SELECT id, uuid, payload, opprettet_dato, har_feilet, antall_forsok, siste_forsok_dato, prosessert_dato FROM ad_outbox"

    val hentUprosesserteSQL = """
            SELECT id, uuid, payload, opprettet_dato, har_feilet, antall_forsok, siste_forsok_dato, prosessert_dato 
            FROM ad_outbox
            WHERE prosessert_dato is null AND opprettet_dato <= ?
            ORDER BY opprettet_dato ASC
            LIMIT ?
        """.trimIndent()

    val markerSomProsessertSQL = """UPDATE ad_outbox SET prosessert_dato = ? WHERE id = ?"""

    val markerSomFeiletSQL =
        """UPDATE ad_outbox SET har_feilet = ?, antall_forsok = ?, siste_forsok_dato = ? WHERE id = ?"""

    val deleteAllSQL = """DELETE from ad_outbox"""

    private fun ResultSet.mapToEntity() = AdOutbox(
        id = this.getLong("id"),
        uuid = this.getString("uuid"),
        payload = this.getString("payload"),
        opprettetDato = this.getObject("opprettet_dato", LocalDateTime::class.java),
        harFeilet = this.getBoolean("har_feilet"),
        antallForsøk = this.getInt("antall_forsok"),
        sisteForsøkDato = this.getObject("siste_forsok_dato", LocalDateTime::class.java),
        prosessertDato = this.getObject("prosessert_dato", LocalDateTime::class.java)
    )

    override fun hentAlle(): List<AdOutbox> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection
                .prepareStatement(selectSQL).executeQuery()
                .use { generateSequence { if (it.next()) it.mapToEntity() else null }.toList() }
        }
    }

    override fun slettAlle() {
        txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(deleteAllSQL).executeUpdate()
        }
    }

    override fun lagre(entity: AdOutbox): Int {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(lagreSQL).apply {
                setString(1, entity.uuid)
                setString(2, entity.payload)
                setTimestamp(3, entity.opprettetDato.toTimeStamp())
                setBoolean(4, entity.harFeilet)
                setInt(5, entity.antallForsøk)
                setTimestamp(6, entity.sisteForsøkDato?.toTimeStamp())
                setTimestamp(7, entity.prosessertDato?.toTimeStamp())
            }.executeUpdate()
        }
    }

    override fun hentUprosesserteMeldinger(batchSize: Int, outboxDelay: Long): List<AdOutbox> {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(hentUprosesserteSQL)
                .apply {
                    setTimestamp(1, LocalDateTime.now().minusSeconds(outboxDelay).toTimeStamp())
                    setInt(2, batchSize)
                }
                .use {
                    val rs = it.executeQuery()
                    return@doInTransaction generateSequence { if (rs.next()) rs.mapToEntity() else null }.toList()
                }
        }
    }

    override fun markerSomProsessert(adOutbox: AdOutbox): Boolean {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(markerSomProsessertSQL).apply {
                setTimestamp(1, adOutbox.prosessertDato?.toTimeStamp())
                setLong(2, adOutbox.id!!)
            }.executeUpdate() > 0
        }
    }

    override fun markerSomFeilet(adOutbox: AdOutbox): Boolean {
        return txTemplate.doInTransaction { ctx ->
            val connection = ctx.connection()
            connection.prepareStatement(markerSomFeiletSQL).apply {
                setBoolean(1, adOutbox.harFeilet)
                setInt(2, adOutbox.antallForsøk)
                setTimestamp(3, adOutbox.sisteForsøkDato?.toTimeStamp())
                setLong(4, adOutbox.id!!)
            }.executeUpdate() > 0
        }
    }

    private fun LocalDateTime.toTimeStamp(): Timestamp {
        return Timestamp.valueOf(this)
    }
}
