package no.nav.arbeidsplassen.importapi.repository

data class PamImportSortable(val property: Property, val direction: Direction) {

    enum class Direction {
        ASC, DESC
    }

    enum class Property {
        UPDATED, CREATED
    }
}
