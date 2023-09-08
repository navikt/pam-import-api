package no.nav.arbeidsplassen.importapi.dto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CategoryDTOTest {
    @Test
    fun `Styrk code validation`() {
        assertTrue(CategoryDTO("1234", CategoryType.STYRK08).validCode())
        assertTrue(CategoryDTO("1", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("0", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("0000", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("9999", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("notNumber", CategoryType.STYRK08).validCode())
        assertFalse(CategoryDTO("", CategoryType.STYRK08).validCode())
    }

    @Test
    fun `Pyrk code validation`() {
        assertFalse(CategoryDTO("0000", CategoryType.PYRK20).validCode())
        assertFalse(CategoryDTO("00000", CategoryType.PYRK20).validCode())
        assertFalse(CategoryDTO("0", CategoryType.PYRK20).validCode())
        assertTrue(CategoryDTO("1", CategoryType.PYRK20).validCode())
        assertTrue(CategoryDTO("9999", CategoryType.PYRK20).validCode())
        assertTrue(CategoryDTO("0310", CategoryType.PYRK20).validCode())
        assertFalse(CategoryDTO("notNumber", CategoryType.PYRK20).validCode())
        assertFalse(CategoryDTO("", CategoryType.PYRK20).validCode())
    }
}
