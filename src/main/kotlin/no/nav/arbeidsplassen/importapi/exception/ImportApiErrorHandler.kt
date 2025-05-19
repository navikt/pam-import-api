package no.nav.arbeidsplassen.importapi.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import io.javalin.Javalin
import io.javalin.http.HttpStatus
import java.util.UUID
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.CONFLICT
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.INVALID_VALUE
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.MISSING_PARAMETER
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.NOT_FOUND
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.PARSE_ERROR
import no.nav.arbeidsplassen.importapi.exception.ImportApiError.ErrorType.UNKNOWN
import no.nav.arbeidsplassen.importapi.security.ForbiddenException
import no.nav.arbeidsplassen.importapi.security.NotFoundException
import no.nav.arbeidsplassen.importapi.security.UnauthorizedException
import org.slf4j.LoggerFactory

object ImportApiErrorHandler {

    data class ErrorMessage(
        val message: String,
        val errorType: ImportApiError.ErrorType,
        val errorRef: UUID = UUID.randomUUID()
    )

    // Global error logger for errorhandler
    private val LOG = LoggerFactory.getLogger("HttpRequestErrorHandler")

    fun Javalin.importApiErrorHandler(): Javalin {
        return this.exception(ImportApiError::class.java) { e, ctx ->
            val message: ErrorMessage = createMessage(e)
            LOG.warn("$message", e)
            val status: HttpStatus = when (e.type) {
                NOT_FOUND -> HttpStatus.NOT_FOUND
                MISSING_PARAMETER, INVALID_VALUE, PARSE_ERROR -> HttpStatus.BAD_REQUEST
                CONFLICT -> HttpStatus.CONFLICT
                UNKNOWN -> HttpStatus.INTERNAL_SERVER_ERROR
            }
            ctx.status(status).json(message)
        }.exception(JsonProcessingException::class.java) { e, ctx ->
            val message: ErrorMessage = handleJsonProcessingException(e)
            LOG.warn("$message", e)
            ctx.status(HttpStatus.BAD_REQUEST).json(message)
        }
            // TODO: Alle disse bør jeg kanskje fjerne?
            .exception(NotFoundException::class.java) { e, ctx ->
                LOG.info("NotFoundException: ${e.message}", e)
                ctx.status(404).result(e.message ?: "")
            }.exception(ForbiddenException::class.java) { e, ctx ->
                LOG.info("ForbiddenException: ${e.message}", e)
                ctx.status(403).result(e.message ?: "")
            }.exception(UnauthorizedException::class.java) { e, ctx ->
                LOG.info("UnauthorizedException: ${e.message}", e)
                ctx.status(401).result(e.message ?: "")
            }.exception(IllegalArgumentException::class.java) { e, ctx ->
                LOG.info("IllegalArgumentException: ${e.message}", e)
                ctx.status(400).result(e.message ?: "")
            }.exception(Exception::class.java) { e, ctx ->
                LOG.info("Exception: ${e.message}", e)
                ctx.status(500).result(e.message ?: "")
            }
    }

    private fun createMessage(error: ImportApiError) = ErrorMessage(message = error.message!!, errorType = error.type)

    // Alle disse returnerer badRequest (400) og bør logges som Warn mener jeg
    private fun handleJsonProcessingException(error: JsonProcessingException): ErrorMessage {
        return when (error) {
            is JsonParseException ->
                ErrorMessage("Parse error: at ${error.location}", PARSE_ERROR)

            is InvalidFormatException ->
                ErrorMessage(
                    "Invalid value: ${error.value} at field ${feltFraPathReference(error.pathReference)}",
                    INVALID_VALUE
                )

            is ValueInstantiationException ->
                ErrorMessage(
                    "Wrong value: ${error.message} at field ${
                        feltFraPathReference(
                            error.pathReference
                        )
                    }", INVALID_VALUE
                )

            is InvalidNullException ->
                ErrorMessage(
                    "Missing parameter: ${error.propertyName.simpleName}",
                    MISSING_PARAMETER
                )

            is MismatchedInputException ->
                ErrorMessage(
                    "Missing parameter: ${feltFraPathReference(error.pathReference)}",
                    MISSING_PARAMETER
                )

            else -> ErrorMessage("Bad Json: ${error.localizedMessage}", UNKNOWN)
        }
    }

}


fun feltFraPathReference(pathReference: String): String {
    val regex = """\["(.*?)"]""".toRegex() // Matcher grupper i hakeparantes
    val matches = regex.findAll(pathReference).map { it.groupValues[1] }.toList()
    return matches.lastOrNull() ?: pathReference
}
