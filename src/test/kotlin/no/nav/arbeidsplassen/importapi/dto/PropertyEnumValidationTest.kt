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
            put(remote, "Hjemmekontor")
        }
        propertyEnumValidation.checkOnlyValidValues(correct)
        val errors = HashMap<PropertyNames,Any>().apply {
            put(extent, "Heltid/Deltid")
            put(engagementtype, "Fast")
            put(jobarrangement, "Skift")
            put(workday, "Ukedager")
            put(workhours, "Kveld")
            put(sector, "Privat")
            put(remote, "Et Kontor")
        }
        val error = assertThrows<ImportApiError> { propertyEnumValidation.checkOnlyValidValues(errors) }
        assertEquals(ErrorType.INVALID_VALUE, error.type)
    }

    @Test
    fun `Properties that support multiple values are validated correctly`() {
        val stringifyedLists = hashMapOf(
            workday to """["Ukedager", "Lørdag", "Søndag"]""",
            workhours to """["Dagtid","Kveld" ,"Natt"]""",
            workLanguage to """["Norsk","Engelsk"]"""
        )
        propertyEnumValidation.checkOnlyValidValues(stringifyedLists)

        val stringifiedListsWithSingleValue = hashMapOf(
            workday to """["Ukedager"]""",
            workhours to """["Dagtid"]""",
            workLanguage to """["Norsk"]"""
        )
        propertyEnumValidation.checkOnlyValidValues(stringifiedListsWithSingleValue)

        val stringifyedListsWithBadValues = hashMapOf(
            workday to """["Ukebager", "Blørdag"]""",
            workhours to """["Kveldd"]""",
            workLanguage to """["Torsk"]"""
        )

        val error = assertThrows<ImportApiError> { propertyEnumValidation.checkOnlyValidValues(stringifyedListsWithBadValues) }
        assertEquals(ErrorType.INVALID_VALUE, error.type)
    }

    @Test
    fun `Properties that support multiple values still support single value`() {
        val singleValue = hashMapOf(workday to "Ukedager", workhours to "Dagtid")
        propertyEnumValidation.checkOnlyValidValues(singleValue)
    }

    @Test
    fun `Properties that dont support multiple values throw exception`() {
        val valuesThatDoesntSupportMultipleValues = hashMapOf(sector to """["Offentlig", "Privat"]""")

        val error = assertThrows<ImportApiError> { propertyEnumValidation.checkOnlyValidValues(valuesThatDoesntSupportMultipleValues) }
        assertEquals(ErrorType.INVALID_VALUE, error.type)
    }
}
