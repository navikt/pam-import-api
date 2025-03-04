package no.nav.arbeidsplassen.importapi.adoutbox

import java.time.LocalDateTime

data class AdOutbox(
    var id: Long? = null,
    val uuid: String,
    val payload: String,
    val opprettetDato: LocalDateTime = LocalDateTime.now(),
    val harFeilet: Boolean = false,
    val antallForsøk: Int = 0,
    val sisteForsøkDato: LocalDateTime? = null,
    val prosessertDato: LocalDateTime? = null,
)
