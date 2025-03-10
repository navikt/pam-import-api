package no.nav.arbeidsplassen.importapi.repository

data class PamImportSlice<T>(
    val content: List<T>,
    val pageable: PamImportPageable
) : Iterable<T> {

    fun nextPageable(): PamImportPageable {
        return pageable.next()
    }

    fun hasNextPage(): Boolean {
        return !empty
    }

    fun previousPageable(): PamImportPageable {
        return pageable.previous()
    }

    fun hasPreviousPage(): Boolean {
        return pageable.number > 0
    }

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

    override fun iterator(): Iterator<T> {
        return content.iterator()
    }

    fun <T2> map(function: (T) -> T2): PamImportSlice<T2> {
        return PamImportSlice(
            this.content.map(function),
            this.pageable
        )
    }
}
