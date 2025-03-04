package no.nav.arbeidsplassen.importapi.repository


import jakarta.inject.Singleton
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import org.slf4j.LoggerFactory

/**
 * TxTemplate er løst basert på ideen bak spring TransactionTemplate – bare mye enklere.
 * Bruksmønsteret er slik:
 *
 * ```
 * txTemplate = TxTemplate(HikariDataSource(...))
 *
 * txTemplate.doInTransaction { ctx ->
 *   val conn = ctx.connection()  // IKKE LUKK CONNECTION ETTER BRUK
 *   conn.prepareStatement("heftig sql kode her").apply {
 *      it.setObject(verdier som skal inn i spørringen)
 *   }.use { statement ->
 *      val resultSet = statement.executeQuery()
 *      if (verden_faller_i_grus_vi_må_rulle_tilbake())
 *          ctx.setRollbackOnly()
 *      return@doInTransaction doSomethingWithResultSet(resultSet)
 *   }
 * }
 * ```
 * Internt ligger transaksjonskonteksten i en ScopedValue. Dette er en eksperimentell feature i java 21-23
 * som håndterer lettvektstråder (LWT) bedre enn ThreadLocal.
 * Default transaksjonssemantikk er PropagationRequired. Dvs at det blir startet en ny transaksjon hvis det ikke allerede finnes
 * en pågående transaksjon. Hvis det finnes en pågående transaksjon, så blir ny transaksjon del av eksisterende.
 *
 * Det er mulig å eksplisitt spesifisere at en nestet transasaksjon skal starte en ny transaksjon (PropagationRequiresNew)
 *
 * Ved hissig bruk av corutiner, der én transaksjon spenner over flere corutiner, kan det bli problemer.
 * Hvis du ikke yielder, eller bruker en LWT dispatcher, så bør det gå greit.
 *
 * Her er du ute i ruskete farvann. Det er en risiko for at transaksjonskonteksten
 * vil flyte mellom corutinene – altså noe som vil resultere i sporadiske og vanskelige feil.
 *
 * Prinsipper:
 *  - Du får JDBC connection fra TxTemplate
 *  - Du lukker ikke JDBC connection selv
 *  - Du lukker ressurser du bruker fra connection (Statement/PreparedStatement)
 *  - TxTemplate holder styr på context og om det allerede eksisterer en transaksjon
 *
 *
 * Likheter og forskjeller fra Spring TransactionTemplate
 * - TxTemplate forholder seg kun til JDBC connection fra en DataSource. Det er kun testet med postgresql og HikariCP
 *   Spring TransactionTemplate er supergenerisk og er i stand til å joine transaksjoner fra andre datakilder (f.eks køer)
 * - Både Spring TxTemplate har default propagation=PropagationRequired, og begge har mulighet for PropagationRequiresNew
 * - Både Spring og TxTemplate baserer seg på at du må utføre det som skal gjøres i en transaksjon innenfor en
 *   doInTransaction() blokk.
 * - Spring tilbyr annotasjoner og aspekter for å abstrahere bort doInTransaction - TxTemplate vil aldri gjøre det
 * - Spring lagrer transaction context i en ThreadLocal slik at du ikke trenger å propagere context selv
 *   TxTemplate bruker ScopedValue til dette, noe som fungerer bedre med virtuelle tråder og som er tryggere mtp lekkasje.
 *
 */
@Singleton
class TxTemplate(private val ds: DataSource) {
    companion object {
        private val LOG = LoggerFactory.getLogger(TxTemplate::class.java)
        private val scopedValueTxContext = ScopedValue.newInstance<TxContext>()
    }

    fun <R> doInTransactionNullable(
        propagation: TxPropagation = TxPropagation.REQUIRED,
        txBlock: (ctx: TxContext) -> R
    ): R? {
        val isNestedTransaction = scopedValueTxContext.isBound && propagation == TxPropagation.REQUIRED
        val conn = if (isNestedTransaction) scopedValueTxContext.get().connection() else ds.connection

        val autocommit = conn.autoCommit
        conn.autoCommit = false

        val ctx = if (isNestedTransaction) scopedValueTxContext.get() else TxContext(conn)

        // NB: ScopedValue.where er litt endret fra java 21 til 23.
        //     I Java 21 så funker ScopedValue.callWhere helt fint, i 23 må det deles opp
        return ScopedValue.callWhere(scopedValueTxContext, ctx) {
            var result: R? = null
            var resultEx: Throwable? = null
            try {
                result = txBlock(ctx)
            } catch (e: Exception) {
                LOG.warn("Exception nådde TxTemplate. Ruller tilbake transaksjon: ${e.message}", e)
                ctx.setRollbackOnly()
                resultEx = e
            }

            if (!isNestedTransaction) {
                conn.use { c ->
                    if (ctx.isRollbackOnly())
                        c.rollback()
                    else
                        c.commit()
                    c.autoCommit = autocommit
                }
            }
            if (result == null && resultEx != null)
                throw resultEx
            result
        }
    }

    fun <R> doInTransaction(
        propagation: TxPropagation = TxPropagation.REQUIRED,
        txBlock: (ctx: TxContext) -> R
    ): R {
        val msg =
            "Uventet resultat, mottok null. Antageligvis programmeringsfeil, vurder å bruke doInTransaction direkte."
        return doInTransactionNullable(propagation, txBlock) ?: throw IllegalStateException(msg)
    }

}

enum class TxPropagation {
    /** REQUIRED er default propagation. Da vil det startes en ny
     * transaksjon hvis det ikke finnes en fra før. Hvis det finnes en fra før så blir
     * dette en del av den eksisterende transaksjonen
     */
    REQUIRED,

    /**
     * REQUIRES_NEW skal du tenke deg godt om før du bruker. Det er som regel feil å
     * bruke dette. REQUIRES_NEW vil starte en ny transaksjon selv om det finnes en fra før.
     * Disse transaksjonene er uavhengig av hverandre.
     */
    REQUIRES_NEW
}

class TxContext(private val conn: Connection) {
    private var rollbackOnly = false

    fun setRollbackOnly() {
        rollbackOnly = true
    }

    fun isRollbackOnly() = rollbackOnly
    fun connection() = conn
}


/**
 * Utilityobjekt som er ment som en fattig trøst for de som savner Spring NamedParameterJdbcTemplate.
 * Dette er svært enkelt uten noen forsøk på smart eller magisk logikk. Det er basert på enkel substituering
 * av :verdi: til ? i en streng, og en måte å holde orden på hvilken ordinal verdien skal ha.
 *
 * Forventet bruk/oppførsel:
 *
 * ```
 * val params = mapOf<String, (pstmt: PreparedStatement, pos: Int) -> Unit>(
 *     Pair(":bar:") { pstmt, pos -> pstmt.setString(pos, "bar_verdi") },
 *     Pair(":baz:") { pstmt, pos -> pstmt.setString(pos, "baz_verdi") }
 *   )
 * val preparedStatement = prepareStatement(connection,
 *      "select * from foo where bar=:bar: and baz=:baz: and gazonk > :bar:",
 *      params
 *    )
 * preparedStatement.execute()...
 * ```
 *
 * her er forventet oppførsel at prapareStatement returnerer et PreparedStatement med følgende SQL:
 * ```
 * "select * from foo where bar=? and baz=? and gazonk > ?"
 * ```
 * og at følgende kode har kjørt:
 * ```
 * pstmt.setString(1, "bar_verdi")
 * pstmt.setString(2, "baz_verdi")
 * pstmt.setString(3, "bar_verdi")
 * ```
 */
object PSTMTUtil {
    fun prepareStatement(
        conn: Connection,
        sql: String,
        params: Map<String, (pstmt: PreparedStatement, pos: Int) -> Unit>
    ): PreparedStatement {
        val preparedSql = sql.replace(Regex(":[^:]+:"), "?")
        val pstmt = conn.prepareStatement(preparedSql)

        val placeholders = findPlaceholders(sql)
        applyParams(params, placeholders, pstmt)

        return pstmt
    }

    private fun applyParams(
        params: Map<String, (pstmt: PreparedStatement, pos: Int) -> Unit>,
        placeholders: List<String>,
        pstmt: PreparedStatement
    ) {
        params.forEach { me ->
            var pos = 0
            placeholders.forEach { p ->
                pos++
                if (p == me.key)
                    me.value(pstmt, pos)
            }
        }
    }

    private fun findPlaceholders(sql: String): List<String> =
        Regex(":[^:]+:").findAll(sql).map { mr ->
            mr.value
        }.toList()
}
