package no.nav.arbeidsplassen.importapi.dto

import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest
class ValidationTest(private val dtoValidation: DTOValidation) {

    @Test
    fun jsonParseTest() {
        val jsonStream = ValidationTest::class.java.getResourceAsStream("/transfer-ads-parse-error.json")
        val error = assertThrows<ValidationError> { dtoValidation.parseJson(jsonStream) }
        assertEquals(ErrorType.PARSE_ERROR, error.type)
    }

    @Test
    fun jsonMissingValueTest() {
        val jsonStream = ValidationTest::class.java.getResourceAsStream("/transfer-ads-missing.json")
        val error = assertThrows<ValidationError> { dtoValidation.parseToDTO(dtoValidation.parseJson(jsonStream))}
        assertEquals(ErrorType.MISSING_PARAMETER, error.type)
    }

}
