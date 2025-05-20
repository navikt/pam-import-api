package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
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

    fun getPropertyValidValues(ctx: Context) {
        ctx.status(HttpStatus.OK).json(propertyNameValueValidation.validValues)
    }

    fun getPropertyNames(ctx: Context) {
        ctx.status(HttpStatus.OK).json(PropertyNames.values())
    }
}
