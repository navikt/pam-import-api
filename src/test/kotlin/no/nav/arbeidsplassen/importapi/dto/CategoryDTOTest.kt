package no.nav.arbeidsplassen.importapi.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryDTOTest {
    @Test
    fun `Code is valid`() {
        assertTrue(CategoryDTO("1234", CategoryType.STYRK08).validCode())
        assertTrue(CategoryDTO("0000", CategoryType.PYRK20).validCode())
        assertTrue(CategoryDTO("0", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("0000", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("9999", CategoryType.STYRK08).validCode())
    }
}
