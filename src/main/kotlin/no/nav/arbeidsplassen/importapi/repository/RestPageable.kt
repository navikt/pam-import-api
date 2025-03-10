package no.nav.arbeidsplassen.importapi.repository

data class RestPageable(
    val size: Int,
    val number: Long,
    val sort: RestSortable
)

data class RestSortable(
    val orderBy: List<RestOrderBy>
)

data class RestOrderBy(
    val property: String, val direction: String, val ignoreCase: Boolean, val ascending: Boolean
)
