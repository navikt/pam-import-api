package no.nav.arbeidsplassen.importapi.dto

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import no.nav.arbeidsplassen.importapi.properties.PropertyNames.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

@MicronautTest
class PropertyEnumValidationTest(private val propertyEnumValidation: PropertyNameValueValidation) {

    @Test
    fun validatePropertyNameValue() {
        val correct = HashMap<PropertyNames,Any>().apply {
            put(extent, "Heltid")
            put(engagementtype, "Fast")
            put(jobarrangement, "Skift")
            put(workday, "Ukedager")
            put(workhours, "Kveld")
            put(sector, "Privat")
        }
        propertyEnumValidation.checkOnlyValidValues(correct)
        val errors = HashMap<PropertyNames,Any>().apply {
            put(extent, "Heltid/Deltid")
            put(engagementtype, "Fast")
            put(jobarrangement, "Skift")
            put(workday, "Ukedager")
            put(workhours, "Kveld")
            put(sector, "Privat")
        }
        val error = assertThrows<ImportApiError> { propertyEnumValidation.checkOnlyValidValues(errors) }
        assertEquals(ErrorType.INVALID_VALUE, error.type)
    }
}
