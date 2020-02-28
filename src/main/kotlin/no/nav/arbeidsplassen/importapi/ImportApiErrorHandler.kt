package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import no.nav.arbeidsplassen.importapi.ErrorType.*
import javax.inject.Singleton;

@Produces
@Singleton
class ImportApiErrorHandler : ExceptionHandler<Throwable, HttpResponse<ErrorMessage>> {

    override fun handle(request: HttpRequest<*>, error: Throwable): HttpResponse<ErrorMessage> {
        return when (error) {
            is ImportApiError -> return when (error.type) {
                NOT_FOUND -> HttpResponse.notFound(createMessage(error))
                MISSING_PARAMETER, INVALID_VALUE, PARSE_ERROR -> HttpResponse.badRequest(createMessage(error))
                CONFLICT -> HttpResponseFactory.INSTANCE.status<ErrorMessage>(HttpStatus.CONFLICT).body(createMessage(error))
                UNKNOWN -> HttpResponse.serverError(createMessage(error))
            }
            is JsonParseException
                -> HttpResponse.badRequest(ErrorMessage("Parse error: ${error.localizedMessage}", PARSE_ERROR.name))
            is MissingKotlinParameterException
                -> HttpResponse.badRequest(ErrorMessage("Missing parameter: ${error.parameter.name}", MISSING_PARAMETER.name))
            is InvalidFormatException
                -> HttpResponse.badRequest(ErrorMessage("Invalid value: ${error.localizedMessage}", INVALID_VALUE.name))
            else -> HttpResponse.serverError(ErrorMessage(error.message, UNKNOWN.name))
        }
    }

    private fun createMessage(error: ImportApiError) = ErrorMessage(message = error.message, errorCode = error.type.name)
}

enum class ErrorType {
    PARSE_ERROR, MISSING_PARAMETER, INVALID_VALUE, CONFLICT, NOT_FOUND, UNKNOWN
}

class ImportApiError(message: String, val type: ErrorType) : Throwable(message)

class ErrorMessage(val message: String?, val errorCode: String)