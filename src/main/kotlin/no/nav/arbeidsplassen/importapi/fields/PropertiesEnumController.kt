package no.nav.arbeidsplassen.importapi.fields

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.arbeidsplassen.importapi.dto.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.dto.PropertyNames
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/properties")
class PropertiesEnumController(private val propertyNameValueValidation: PropertyNameValueValidation) {

    @Get("/values")
    fun getPropertyValidValues(): HashMap<String, Set<String>> {
        return propertyNameValueValidation.validValues
    }

    @Get("/names")
    fun getPropertyNames(): Array<PropertyNames> {
        return PropertyNames.values()
    }

}