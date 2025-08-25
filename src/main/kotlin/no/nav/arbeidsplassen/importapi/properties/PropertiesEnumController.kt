package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiAdditionalContent
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiContentProperty
import io.javalin.openapi.OpenApiResponse
import no.nav.arbeidsplassen.importapi.config.JavalinController
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PropertiesEnumController(private val propertyNameValueValidation: PropertyNameValueValidation) :
    JavalinController {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(PropertiesEnumController::class.java)
        private fun Context.sortParam(): String = queryParam("sort") ?: "code"
    }

    override fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/properties/values", { getPropertyValidValues(it) })
        javalin.get("/api/v1/properties/names", { getPropertyNames(it) })
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/properties/values",
        methods = [HttpMethod.GET],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getPropertyValidValues 200 response. The OpenAPI schema only show a couple of the values returned.",
                content = [OpenApiContent(
                    mimeType = "application/map-string-object",
                    from = Any::class,
                    properties = [
                        OpenApiContentProperty(
                            name = "workLanguage",
                            from = Array<String>::class
                        ),
                        OpenApiContentProperty(
                            name = "extent",
                            from = Array<String>::class
                        )],
                    additionalProperties = OpenApiAdditionalContent(
                        from = Array<String>::class
                    ),
                    example = """
                       {
                          "workLanguage": {
                            "Norsk", 
                            "Engelsk", 
                            "Skandinavisk", 
                            "Samisk"
                          },
                          "extent": {
                            "heltid",
                            "deltid"
                          }
                       }
                    """,
                )]
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
                content = [OpenApiContent(from = PropertyNames::class)]
            ),
        ]
    )
    fun getPropertyNames(ctx: Context) {
        ctx.status(HttpStatus.OK).json(PropertyNames.values())
    }
}
