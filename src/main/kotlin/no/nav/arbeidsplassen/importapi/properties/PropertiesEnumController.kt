package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/properties")
class PropertiesEnumController(private val propertyNameValueValidation: PropertyNameValueValidation) {

    @Get("/values")
    fun getPropertyValidValues(): HashMap<PropertyNames, Set<String>> {
        return propertyNameValueValidation.validValues
    }

    @Get("/names")
    fun getPropertyNames(): Array<PropertyNames> {
        return PropertyNames.values()
    }

}
