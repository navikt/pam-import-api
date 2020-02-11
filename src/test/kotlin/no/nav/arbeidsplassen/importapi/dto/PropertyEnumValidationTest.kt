package no.nav.arbeidsplassen.importapi.dto

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

@MicronautTest
class PropertyEnumValidationTest(private val propertyEnumValidation: PropertyNameValueValidation) {

    @Test
    fun validatePropertyNameValue() {
        val correct = HashMap<String,Any>().apply {
            put("extent", "Heltid")
            put("engagementtype", "Fast")
            put("jobarrangement", "Skift")
            put("workday", "Ukedager")
            put("workhours", "Kveld")
            put("sector", "Privat")
        }
        propertyEnumValidation.checkOnlyValidValues(correct)
        val errors = HashMap<String,Any>().apply {
            put("extent", "Heltid/Deltid")
            put("engagementtype", "Fast")
            put("jobarrangement", "Skift")
            put("workday", "Ukedager")
            put("workhours", "Kveld")
            put("sector", "Privat")
        }
        val error = assertThrows<ValidationError> { propertyEnumValidation.checkOnlyValidValues(errors) }
        assertEquals(ErrorType.INVALID_VALUE, error.type)
    }
}