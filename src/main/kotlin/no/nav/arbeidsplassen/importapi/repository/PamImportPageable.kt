package no.nav.arbeidsplassen.importapi.repository

import kotlin.math.max

data class PamImportPageable(
    val size: Int = 100,
    val number: Long = 0L,
    val sort: PamImportSortable = PamImportSortable(
        PamImportSortable.Property.UPDATED,
        PamImportSortable.Direction.ASC
    ),
) {
    fun next(): PamImportPageable = this.copy(number = number + 1)
    fun previous(): PamImportPageable = this.copy(number = max(0, number - 1))
}
