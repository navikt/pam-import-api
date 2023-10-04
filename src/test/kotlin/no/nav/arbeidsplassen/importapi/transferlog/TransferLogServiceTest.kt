package no.nav.arbeidsplassen.importapi.transferlog

import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogServiceTest{

    lateinit var transferLogService : TransferLogService

    @Mock
    val transferLogRepository: TransferLogRepository = mock(TransferLogRepository::class.java)

    @Mock
    val propertyNameValueValidation: PropertyNameValueValidation = mock(PropertyNameValueValidation::class.java)

    @Mock
    val ontologiGateway : LokalOntologiGateway = mock(LokalOntologiGateway::class.java)


    @BeforeAll
    fun setUp() {
        transferLogService = TransferLogService(transferLogRepository, propertyNameValueValidation, ontologiGateway)
    }
    @Test
    fun `Invalid JANZZ code is removed`() {
        val ad = AdDTO(published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("0000", CategoryType.JANZZ,  "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test")).thenReturn(emptyList())

        val result = transferLogService.removeInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(0, result.categoryList.size)
    }

    @Test
    fun `Valid JANZZ code is kept`() {
        val ad = AdDTO(published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("1234", CategoryType.JANZZ,  "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test")).thenReturn(listOf(Typeahead(name="test", code=1234)))

        val result = transferLogService.removeInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(1, result.categoryList.size)
    }
}