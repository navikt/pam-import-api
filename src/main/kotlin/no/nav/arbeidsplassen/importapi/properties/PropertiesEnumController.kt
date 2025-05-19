package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PropertiesEnumController(private val propertyNameValueValidation: PropertyNameValueValidation) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PropertiesEnumController::class.java)
        private fun Context.sortParam(): String = queryParam("sort") ?: "code"
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/properties/values", {
            it.status(HttpStatus.OK).json(getPropertyValidValues())
        })

        javalin.get("/api/v1/properties/names", {
            it.status(HttpStatus.OK).json(getPropertyNames())
        })
    }

    fun getPropertyValidValues(): HashMap<PropertyNames, Set<String>> {
        return propertyNameValueValidation.validValues
    }

    fun getPropertyNames(): Array<PropertyNames> {
        return PropertyNames.values()
    }
}
