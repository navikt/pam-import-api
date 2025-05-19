package no.nav.arbeidsplassen.importapi.exception

class ImportApiError(message: String, val type: ErrorType) : Exception(message) {
    enum class ErrorType {
        PARSE_ERROR, MISSING_PARAMETER, INVALID_VALUE, CONFLICT, NOT_FOUND, UNKNOWN
    }
}
