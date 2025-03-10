package no.nav.arbeidsplassen.importapi.repository

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
