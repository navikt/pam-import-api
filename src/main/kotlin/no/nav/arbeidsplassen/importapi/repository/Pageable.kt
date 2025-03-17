package no.nav.arbeidsplassen.importapi.repository

import kotlin.math.max

data class Pageable(
    val size: Int = 1000,
    val number: Long = 0L,
    val sort: Sortable = Sortable(
        Sortable.Property.UPDATED,
        Sortable.Direction.ASC
    ),
) {
    val offset: Long
        get() = size * number

    fun next(): Pageable = this.copy(number = number + 1)
    fun previous(): Pageable = this.copy(number = max(0, number - 1))
}

data class Sortable(val property: Property, val direction: Direction) {

    enum class Direction {
        ASC, DESC
    }

    enum class Property {
        UPDATED, CREATED
    }
}

data class Slice<T>(
    val content: List<T>,
    val pageable: Pageable
) : Iterable<T> {

    fun nextPageable(): Pageable {
        return pageable.next()
    }

    fun hasNextPage(): Boolean {
        return !empty
    }

    fun previousPageable(): Pageable {
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

    fun <T2> map(function: (T) -> T2): Slice<T2> {
        return Slice(
            this.content.map(function),
            this.pageable
        )
    }
}
