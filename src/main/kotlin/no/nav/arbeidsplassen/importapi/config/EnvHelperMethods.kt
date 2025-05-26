package no.nav.arbeidsplassen.importapi.config

fun Map<String, String>.variable(felt: String) = this[felt] ?: error("$felt er ikke angitt")
fun Map<String, String>.nullableVariable(felt: String) = this[felt]
