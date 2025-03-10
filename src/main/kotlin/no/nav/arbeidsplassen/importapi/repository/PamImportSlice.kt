package no.nav.arbeidsplassen.importapi.repository

data class PamImportSlice<T>(
    val content: List<T>,
    val pageable: PamImportPageable
) : Iterable<T> {

    fun nextPageable(): PamImportPageable {
        return pageable.next()
    }

    fun hasNextPage(): Boolean {
        return !isEmpty
    }

    fun previousPageable(): PamImportPageable {
        return pageable.previous()
    }

    fun hasPreviousPage(): Boolean {
        return pageable.number > 0
    }

    val isEmpty: Boolean
        get() = content.isEmpty()

    val numberOfElements: Int
        get() = content.size

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
