package no.nav.arbeidsplassen.importapi.repository

import no.nav.arbeidsplassen.importapi.adpuls.AdPulsDTO

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

// Denne er her fordi OpenAPI ikke liker generics, brukes i dokumentasjonen
data class RestSliceAdPulsDTO(
    val content: List<AdPulsDTO>,
    val pageable: RestPageable
) {

    val empty: Boolean
        get() = content.isEmpty()

    val numberOfElements: Int
        get() = content.size

    val pageNumber: Long
        get() = pageable.number

    val size: Int
        get() = pageable.size

    val offset: Long
        get() = (pageable.number * pageable.size)
}


data class RestSlice<T>(
    val content: List<T>,
    val pageable: RestPageable
) {

    val empty: Boolean
        get() = content.isEmpty()

    val numberOfElements: Int
        get() = content.size

    val pageNumber: Long
        get() = pageable.number

    val size: Int
        get() = pageable.size

    val offset: Long
        get() = (pageable.number * pageable.size)
}
