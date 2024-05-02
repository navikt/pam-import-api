package no.nav.arbeidsplassen.importapi.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocationDTOTest {

    @Test
    fun isCountryAbroad() {
        assertFalse(LocationDTO(country = "Norge").isCountryAbroad())
        assertFalse(LocationDTO(country = "Norway").isCountryAbroad())
        assertFalse(LocationDTO(country = "Noreg").isCountryAbroad())
        assertFalse(LocationDTO(country = "NO").isCountryAbroad())
        assertFalse(LocationDTO(country = "").isCountryAbroad())
        assertFalse(LocationDTO(country = null).isCountryAbroad())

        assertTrue(LocationDTO(country = "Sverige").isCountryAbroad())
        assertTrue(LocationDTO(country = "Danmark").isCountryAbroad())
    }
}
