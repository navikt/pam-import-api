package no.nav.arbeidsplassen.importapi.config

data class NavSlice<T>(
    val content: List<T>,
    val pageable: NavPageable
) : Iterable<T> {

    fun nextPageable(): NavPageable {
        return pageable.next()
    }

    fun previousPageable(): NavPageable {
        return pageable.previous()
    }

    val isEmpty: Boolean
        get() = content.isEmpty()

    val numberOfElements: Int
        get() = content.size

    override fun iterator(): Iterator<T> {
        return content.iterator()
    }

    fun <T2> map(function: (T) -> T2): NavSlice<T2> {
        return NavSlice(
            this.content.map(function),
            this.pageable
        )
    }
}
