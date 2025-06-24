package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PropertiesEnumController(private val propertyNameValueValidation: PropertyNameValueValidation) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(PropertiesEnumController::class.java)
        private fun Context.sortParam(): String = queryParam("sort") ?: "code"
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/properties/values", { getPropertyValidValues(it) })
        javalin.get("/api/v1/properties/names", { getPropertyNames(it) })
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/properties/values",
        methods = [HttpMethod.GET],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getPropertyValidValues 200 response",
                content = [OpenApiContent(from = Map::class)] // TODO Must fix
            ),
        ]
    )
    fun getPropertyValidValues(ctx: Context) {
        ctx.status(HttpStatus.OK).json(propertyNameValueValidation.validValues)
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/properties/names",
        methods = [HttpMethod.GET],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getPropertyNames 200 response",
                content = [OpenApiContent(from = Array<PropertyNames>::class)] // TODO Denne blir ikke helt lik som i Micronaut
            ),
        ]
    )
    fun getPropertyNames(ctx: Context) {
        ctx.status(HttpStatus.OK).json(PropertyNames.values())
    }
}
