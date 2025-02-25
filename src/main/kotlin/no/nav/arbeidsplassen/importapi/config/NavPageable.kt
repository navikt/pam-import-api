package no.nav.arbeidsplassen.importapi.config

data class NavPageable(
    val limit: Int = 100,
    val offset: Long = 0L,
) {
    fun next() : NavPageable = this.copy(offset = offset + limit)
    fun previous() : NavPageable = this.copy(offset = offset - limit)
    val number: Int
        get() = (offset / limit).toInt()
    val size: Int
        get() = limit
}
