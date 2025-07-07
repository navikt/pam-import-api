package no.nav.arbeidsplassen.importapi.transferlog

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogServiceITest : TestRunningApplication() {

    private val transferLogService: TransferLogService = appCtx.servicesApplicationContext.transferLogService
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun `test expires updates to 10 days after published if applicationdue snarest and expires null`() {
        txTemplate.doInTransactionNullable { ctx ->

            val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "Snarest")
            val publishedDate = LocalDateTime.now()
            val ad = AdDTO(
                published = publishedDate, properties = propertiesAd, expires = null,
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                    LocationDTO(postalCode = "0123")
                )
            )

            assertEquals(
                "Expire skal settes 10 dager fra published dersom den er null og applicationdue er Snarest",
                transferLogService.handleExpiryAndStarttimeCombinations(ad).expires, publishedDate.plusDays(10)
            )

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun `test ad is unchanged when applicationdue is a date`() {
        txTemplate.doInTransactionNullable { ctx ->

            val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "01.05.2019")
            val publishedDate = LocalDateTime.now()
            val ad = AdDTO(
                published = publishedDate, properties = propertiesAd, expires = null,
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                    LocationDTO(postalCode = "0123")
                )
            )

            assertEquals(
                "Dersom applicationdue er en dato skal expire bestå urørt",
                transferLogService.handleExpiryAndStarttimeCombinations(ad).expires, null
            )

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun `test ad is unchanged when expire has value set`() {
        txTemplate.doInTransactionNullable { ctx ->

            val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to "Snarest")
            val expiredate = LocalDateTime.now()
            val ad = AdDTO(
                published = LocalDateTime.now(), properties = propertiesAd, expires = expiredate,
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                    LocationDTO(postalCode = "0123")
                )
            )

            assertEquals(
                "Dersom expire har en verdi skal expire bestå urørt",
                transferLogService.handleExpiryAndStarttimeCombinations(ad).expires, expiredate
            )

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun `test expiry localdate string is set to later than application due`() {
        txTemplate.doInTransactionNullable { ctx ->

            val nextYear = LocalDate.now().year.plus(1)
            val applicationdue = "24.03.".plus(nextYear)
            val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to applicationdue)
            val expiryDateLaterThanApplicationDue = LocalDateTime.of(LocalDate.of(nextYear, 3, 30), LocalTime.now())
            val ad = AdDTO(
                published = LocalDateTime.now(), properties = propertiesAd, expires = expiryDateLaterThanApplicationDue,
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                    LocationDTO(postalCode = "0123")
                )
            )
            assertEquals(
                "Dersom expirydate er lengre enn søknadsfrist, skal den settes til søknadsfrist",
                transferLogService.handleExpiryAndStarttimeCombinations(ad).expires?.toLocalDate(),
                LocalDate.parse(applicationdue, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            )

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun `test expiry localdatetime string is set to later than application due`() {
        txTemplate.doInTransactionNullable { ctx ->

            val nextYear = LocalDate.now().year.plus(1)
            val applicationdue = nextYear.toString().plus("-03-24T00:00:00")
            val propertiesAd: HashMap<PropertyNames, String> = hashMapOf(PropertyNames.applicationdue to applicationdue)
            val expiryDateLaterThanApplicationDue = LocalDateTime.of(LocalDate.of(nextYear, 3, 30), LocalTime.now())
            val ad = AdDTO(
                published = LocalDateTime.now(), properties = propertiesAd, expires = expiryDateLaterThanApplicationDue,
                adText = "adText", employer = EmployerDTO(null, "test", null, LocationDTO()),
                reference = UUID.randomUUID().toString(), title = "title", locationList = listOf(
                    LocationDTO(postalCode = "0123")
                )
            )
            assertEquals(
                "Dersom expirydate er lengre enn søknadsfrist, skal den settes til søknadsfrist",
                transferLogService.handleExpiryAndStarttimeCombinations(ad).expires?.toLocalDate(),
                LocalDate.parse(applicationdue, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            ctx.setRollbackOnly()
        }
    }
}
