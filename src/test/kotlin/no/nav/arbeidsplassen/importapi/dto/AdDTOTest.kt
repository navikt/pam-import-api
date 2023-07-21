package no.nav.arbeidsplassen.importapi.dto

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

@MicronautTest
class AdDTOTest {

    @Test
    fun testAdDTOValidation() {
        AdDTO(reference = "123", published = LocalDateTime.now(), title = "has a title", adText = "has adtext", locationList = listOf(LocationDTO(postalCode = "0123")), expires = LocalDateTime.now(), employer = EmployerDTO(null, "test",null, LocationDTO()))
        assertThrows<IllegalArgumentException> {
            AdDTO(reference = "123", published = LocalDateTime.now(), title = "", adText = "has adtext", locationList = listOf(LocationDTO(postalCode = "0123")), expires = LocalDateTime.now(), employer = null)
        }
        assertThrows<IllegalArgumentException> {
            AdDTO(reference = "123", positions = 0, published = LocalDateTime.now(), title = "has a title", adText = "has adtext", locationList = listOf(LocationDTO(postalCode = "0123")), expires = LocalDateTime.now(), employer = null)
        }
    }

    @Test
    fun testContactDTOValidation() {
        ContactDTO(title = "Test", phone = "123", email = "123", name = "321", role = "role")
        ContactDTO(title=null, name = null, email=null, phone=null, role = null)
        assertThrows<IllegalArgumentException> {
            ContactDTO(title = null, name = null, email = null, phone = "1234567890123456789012345678901234567", role = null)
        }
    }

    @Test
    fun testEmployerDTOValidation() {
        val exception = assertThrows<IllegalArgumentException> {
            AdDTO(reference = "123", published = LocalDateTime.now(), title = "has a title", adText = "has adtext",
                locationList = listOf(LocationDTO(postalCode = "0123")),
                expires = LocalDateTime.now(),
                employer = null)
        }
        assertEquals("Employer should not be empty", exception.message)
    }
}
