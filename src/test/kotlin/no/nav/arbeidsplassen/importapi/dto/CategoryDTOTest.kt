package no.nav.arbeidsplassen.importapi.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryDTOTest {
    @Test
    fun `Styrk code validation`() {
        assertFalse(CategoryDTO("2221", CategoryType.STYRK08).validCode())
    }

    @Test
    fun `Pyrk code validation`() {
        assertFalse(CategoryDTO("2221", CategoryType.PYRK20).validCode())
    }

    @Test
    fun `JANZZ validation`() {
        assertTrue(CategoryDTO("1234", CategoryType.JANZZ).validCode())
    }
}
