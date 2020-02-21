package no.nav.arbeidsplassen.importapi

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import no.nav.arbeidsplassen.importapi.ApiError
import no.nav.arbeidsplassen.importapi.ErrorType.*
import javax.inject.Singleton;

@Produces
@Singleton
class ApiErrorHandler : ExceptionHandler<ApiError, HttpResponse<String>> {

    override fun handle(request: HttpRequest<*>, error: ApiError): HttpResponse<String> {
       return when (error.type) {
           NOT_FOUND -> HttpResponse.notFound(error.message)
           MISSING_PARAMETER, INVALID_VALUE, PARSE_ERROR -> HttpResponse.badRequest(error.message)
           CONFLICT -> HttpResponse.status(HttpStatus.CONFLICT, error.message)
           UNKNOWN -> HttpResponse.serverError(error.message)
        }
    }
}

enum class ErrorType {
    PARSE_ERROR, MISSING_PARAMETER, INVALID_VALUE, CONFLICT, NOT_FOUND, UNKNOWN
}

class ApiError(message: String, val type: ErrorType) : Throwable(message)