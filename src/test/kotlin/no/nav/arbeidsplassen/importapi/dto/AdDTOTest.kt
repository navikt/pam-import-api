package no.nav.arbeidsplassen.importapi.dto

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

@MicronautTest
class AdDTOTest {

    @Test
    fun `AdDTO created ok`() {
        val validAd = validAdDto()
        assertEquals("123", validAd.reference)
    }

    @Test
    fun  `Postions shold be on or more`() {
        val exception = assertThrows<IllegalArgumentException> {
            val test = validAdDto().copy(positions = 0)
        }
        assertEquals("positions should be 1 or more", exception.message)
    }

    @Test
    fun `Contact phone size should not exceed 36`() {
        ContactDTO(title = "Test", phone = "123", email = "123", name = "321", role = "role")
        ContactDTO(title=null, name = null, email=null, phone=null, role = null)
        val exception = assertThrows<IllegalArgumentException> {
            ContactDTO(title = null, name = null, email = null, phone = "1234567890123456789012345678901234567", role = null)
        }
        assertEquals("Contact phone size > 36", exception.message)
    }

    @Test
    fun `Employer should not be empty`() {
        val exception = assertThrows<IllegalArgumentException> {
            val test = validAdDto().copy(employer = null)
        }
        assertEquals("Employer should not be empty", exception.message)
    }

    @Test
    fun `Title should not be blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            val test = validAdDto().copy(title = "")
        }
        assertEquals("title should not be blank", exception.message)
    }

    @Test
    fun `Title should not be longer than 512 characters`() {
        val exception = assertThrows<IllegalArgumentException> {
            val test = validAdDto().copy(title = "1324567890".repeat(51).plus("123"))
        }
        assertEquals("title should not have size > 512 (was 513)", exception.message)
    }

    private fun validAdDto(): AdDTO {
        return AdDTO(
            reference = "123", published = LocalDateTime.now(), title = "has a title", adText = "has adtext",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            expires = LocalDateTime.now(),
            employer = EmployerDTO(null, "test",null, LocationDTO())
        )
    }
}
