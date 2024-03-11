package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

@MicronautTest
class TransferLogServiceITest(private val transferLogService: TransferLogService) {

    @Test
    fun `test expires updates to 10 days after published if applicationdue snarest and expires null`() {
        val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "Snarest")
        val publishedDate = LocalDateTime.now()
        val ad = AdDTO(published = publishedDate, properties = propertiesAd, expires = null,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                LocationDTO(postalCode = "0123")))

        assertEquals("Expire skal settes 10 dager fra published dersom den er null og applicationdue er Snarest",
            transferLogService.handleInvalidExpiryAndStarttimeCombinations(ad).expires, publishedDate.plusDays(10))
    }

    @Test
    fun `test ad is unchanged when applicationdue is a date`() {
        val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "01.05.2019")
        val publishedDate = LocalDateTime.now()
        val ad = AdDTO(published = publishedDate, properties = propertiesAd, expires = null,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                LocationDTO(postalCode = "0123")))

        assertEquals("Dersom applicationdue er en dato skal expire bestå urørt",
            transferLogService.handleInvalidExpiryAndStarttimeCombinations(ad).expires, null)
    }

    @Test
    fun `test ad is unchanged when expire has value set`() {
        val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "Snarest")
        val expiredate = LocalDateTime.now()
        val ad = AdDTO(published = LocalDateTime.now(), properties = propertiesAd, expires = expiredate,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                LocationDTO(postalCode = "0123")))

        assertEquals("Dersom expire har en verdi skal expire bestå urørt",
            transferLogService.handleInvalidExpiryAndStarttimeCombinations(ad).expires, expiredate)
    }

    @Test
    fun `test expiry date is set to later than application due`() {
        val nextYear = LocalDate.now().year.plus(1)
        val applicationdue = "24.03.".plus(nextYear)
        val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to applicationdue)
        val expiryDateLaterThanApplicationDue = LocalDateTime.of(LocalDate.of(nextYear, 3, 30), LocalTime.now())
        val ad = AdDTO(published = LocalDateTime.now(), properties = propertiesAd, expires = expiryDateLaterThanApplicationDue,
            adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
            reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                LocationDTO(postalCode = "0123")))
        assertEquals("Dersom expirydate er lengre enn søknadsfrist, skal den settes til søknadsfrist",
            transferLogService.handleInvalidExpiryAndStarttimeCombinations(ad).expires?.toLocalDate(),
            LocalDate.parse(applicationdue, DateTimeFormatter.ofPattern("dd.MM.yyyy")))
    }
}
