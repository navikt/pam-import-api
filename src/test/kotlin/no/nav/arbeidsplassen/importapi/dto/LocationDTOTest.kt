package no.nav.arbeidsplassen.importapi.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocationDTOTest {

    @Test
    fun hasOnlyCountrySet() {
        assertTrue(LocationDTO(null, null, "Norge", null, null, null, null, null).hasOnlyCountrySet())
        assertTrue(LocationDTO("", "", "Norge", "", "", "", "", "").hasOnlyCountrySet())
        assertFalse(LocationDTO("", "", "", "", "", "", "", "").hasOnlyCountrySet())
        assertFalse(LocationDTO("", "", "Norge", "", "", "Oslo", "", "").hasOnlyCountrySet())
    }
}
