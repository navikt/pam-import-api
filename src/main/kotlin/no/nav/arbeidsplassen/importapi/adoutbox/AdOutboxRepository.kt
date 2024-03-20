package no.nav.arbeidsplassen.importapi.adoutbox

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import no.nav.arbeidsplassen.importapi.provider.toTimeStamp
import java.sql.Connection
import java.sql.ResultSet
import java.time.LocalDateTime
import jakarta.transaction.Transactional

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class AdOutboxRepository(val connection: Connection) : CrudRepository<AdOutbox, Long> {
    open fun ResultSet.toAdOutbox() = AdOutbox(
        id = this.getLong("id"),
        uuid = this.getString("uuid"),
        payload = this.getString("payload"),
        opprettetDato = this.getObject("opprettet_dato", LocalDateTime::class.java),
        harFeilet = this.getBoolean("har_feilet"),
        antallForsøk = this.getInt("antall_forsok"),
        sisteForsøkDato = this.getObject("siste_forsok_dato", LocalDateTime::class.java),
        prosessertDato = this.getObject("prosessert_dato", LocalDateTime::class.java)
    )

    @Transactional
    open fun lagre(entity: AdOutbox): Int {
        val sql = """
            INSERT INTO ad_outbox (uuid, payload, opprettet_dato, har_feilet, antall_forsok, siste_forsok_dato, prosessert_dato)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return connection.prepareStatement(sql).apply {
            setString(1, entity.uuid)
            setString(2, entity.payload)
            setTimestamp(3, entity.opprettetDato.toTimeStamp())
            setBoolean(4, entity.harFeilet)
            setInt(5, entity.antallForsøk)
            setTimestamp(6, entity.sisteForsøkDato?.toTimeStamp())
            setTimestamp(7, entity.prosessertDato?.toTimeStamp())
        }.executeUpdate()
    }

    @Transactional
    open fun hentUprosesserteMeldinger(batchSize: Int = 1000, outboxDelay: Long = 30): List<AdOutbox> {
        val sql = """
            SELECT id, uuid, payload, opprettet_dato, har_feilet, antall_forsok, siste_forsok_dato, prosessert_dato 
            FROM ad_outbox
            WHERE prosessert_dato is null AND opprettet_dato <= ?
            ORDER BY opprettet_dato ASC
            LIMIT ?
        """.trimIndent()

        val resultSet = connection
            .prepareStatement(sql)
            .apply {
                setTimestamp(1, LocalDateTime.now().minusSeconds(outboxDelay).toTimeStamp())
                setInt(2, batchSize)
            }.executeQuery()

        return resultSet.use { generateSequence { if (it.next()) it.toAdOutbox() else null }.toList() }
    }

    @Transactional
    open fun lagreFlere(entities: Iterable<AdOutbox>) = entities.sumOf { lagre(it) }

    @Transactional
    open fun markerSomProsessert(adOutbox: AdOutbox): Boolean {
        val sql = """UPDATE ad_outbox SET prosessert_dato = ? WHERE id = ?"""

        return connection.prepareStatement(sql).apply {
            setTimestamp(1, adOutbox.prosessertDato?.toTimeStamp())
            setLong(2, adOutbox.id!!)
        }.executeUpdate() > 0
    }

    @Transactional
    open fun markerSomFeilet(adOutbox: AdOutbox): Boolean {
        val sql = """UPDATE ad_outbox SET har_feilet = ?, antall_forsok = ?, siste_forsok_dato = ? WHERE id = ?"""

        return connection.prepareStatement(sql).apply {
            setBoolean(1, adOutbox.harFeilet)
            setInt(2, adOutbox.antallForsøk)
            setTimestamp(3, adOutbox.sisteForsøkDato?.toTimeStamp())
            setLong(4, adOutbox.id!!)
        }.executeUpdate() > 0
    }
}
