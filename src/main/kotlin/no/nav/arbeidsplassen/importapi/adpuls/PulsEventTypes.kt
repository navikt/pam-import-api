package no.nav.arbeidsplassen.importapi.adpuls

enum class PulsEventType(val value: String) {
    pageviews("Stilling visning"),
    applicationurlclicks("Stilling sok-via-url"),
    unknown("unknown");

    companion object {
        private val map = PulsEventType.values().associateBy(PulsEventType::value)
        fun fromValue(value: String) = map[value] ?: unknown
    }

}
