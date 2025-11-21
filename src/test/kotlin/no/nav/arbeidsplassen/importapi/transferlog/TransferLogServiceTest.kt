package no.nav.arbeidsplassen.importapi.transferlog

import java.time.LocalDateTime
import java.util.UUID
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryType
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogServiceTest {

    val transferLogRepository: TransferLogRepository = mock(TransferLogRepository::class.java)
    val propertyNameValueValidation: PropertyNameValueValidation = mock(PropertyNameValueValidation::class.java)
    val ontologiGateway: LokalOntologiGateway = mock(LokalOntologiGateway::class.java)
    val transferLogService: TransferLogService =
        TransferLogService(transferLogRepository, propertyNameValueValidation, ontologiGateway)

    @Test
    fun `Invalid JANZZ code category is removed`() {
        val properties = hashMapOf(PropertyNames.keywords to "property1;property2")
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            properties = properties,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("0000", CategoryType.JANZZ, "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test")).thenReturn(emptyList())

        val result = transferLogService.handleInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(0, result.categoryList.size)
        Assertions.assertEquals("test;property1;property2", result.properties[PropertyNames.keywords])
    }

    @Test
    fun `Valid JANZZ code category is kept`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("1234", CategoryType.JANZZ, "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test"))
            .thenReturn(listOf(Typeahead(name = "test", code = 1234)))

        val result = transferLogService.handleInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(1, result.categoryList.size)
    }

    @Test
    fun `STYRK code category is removed but added to searchtags`() {
        val properties = hashMapOf(PropertyNames.keywords to "property1;property2")
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            properties = properties,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("1234", CategoryType.STYRK08, "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test"))
            .thenReturn(listOf(Typeahead(name = "test", code = 1234)))

        val result = transferLogService.handleInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(0, result.categoryList.size)
        Assertions.assertEquals("test;property1;property2", result.properties[PropertyNames.keywords])
    }

    @Test
    fun `PYRK code category is removed but added to searchtags`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(postalCode = "0123")),
            categoryList = listOf(CategoryDTO("1234", CategoryType.PYRK20, "test", "test"))
        )

        Mockito.`when`(ontologiGateway.hentTypeaheadStilling("test"))
            .thenReturn(listOf(Typeahead(name = "test", code = 1234)))

        val result = transferLogService.handleInvalidCategories(ad, 12, "test")
        Assertions.assertEquals(0, result.categoryList.size)
        Assertions.assertEquals("test", result.properties[PropertyNames.keywords])
    }

    @Test
    fun `Accepts ad with country Danmark set`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(country = "Danmark"))
        )

        transferLogService.validate(ad)
    }

    @Test
    fun `Does not accept ad with only country Norge set`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(country = "Norge"))
        )
        val exception = assertThrows<ImportApiError> {
            transferLogService.validate(ad)
        }
        Assertions.assertEquals(
            "Location does not have postal code, or does not have county/municipality",
            exception.message
        )
    }

    @Test
    fun `Does accept ad with only county set`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(county = "Vestland"))
        )
        transferLogService.validate(ad)
    }

    @Test
    fun `Does accept ad with only municipality set`() {
        val ad = AdDTO(
            published = LocalDateTime.now(), expires = LocalDateTime.now(),
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title",
            locationList = listOf(LocationDTO(municipal = "Bergen"))
        )
        transferLogService.validate(ad)
    }
}
