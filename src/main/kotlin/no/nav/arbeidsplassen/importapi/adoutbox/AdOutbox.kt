package no.nav.arbeidsplassen.importapi.adoutbox

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.sql.ResultSet
import java.time.LocalDateTime

@Entity
data class AdOutbox(
    @Id @GeneratedValue var id: Long? = null,
    val uuid: String,
    val payload: String,
    val opprettetDato: LocalDateTime = LocalDateTime.now(),
    val harFeilet: Boolean = false,
    val antallForsøk: Int = 0,
    val sisteForsøkDato: LocalDateTime? = null,
    val prosessertDato: LocalDateTime? = null,
)
