package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferJsonString
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryType
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.Pageable
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.toMD5Hex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@MicronautTest
class TransferLogTasksTest(
    private val transferLogTasks: TransferLogTasks,
    private val transferLogRepository: TransferLogRepository,
    private val providerRepository: ProviderRepository,
    private val objectMapper: ObjectMapper,
    private val adStateRepository: AdStateRepository,
    private val adOutboxRepository: AdOutboxRepository
) {

    @Inject
    lateinit var txTemplate: TxTemplate

    @Test
    fun doTransferLogTaskTest() {
        val payload = objectMapper.transferJsonString()
        val provider = providerRepository.newTestProvider()
        transferLogRepository.save(
            TransferLog(
                providerId = provider.id!!,
                md5 = payload.toMD5Hex(),
                payload = payload,
                items = 2
            )
        )
        transferLogTasks.processTransferLogTask()
        val adstates = adStateRepository.findAll()
        adstates.forEach { println(it.jsonPayload) }
        assertEquals(2, adstates.count())
        val transferLog =
            transferLogRepository.findByStatus(TransferLogStatus.DONE, Pageable(size = 1000, number = 0))
        assertEquals(1, transferLog.count())
        val adOutbox = adOutboxRepository.hentUprosesserteMeldinger(outboxDelay = 0)
        assertEquals(2, adOutbox.size)
    }

    @Test
    fun deleteTransferLogTaskTest() {
        // Jeg måtte innføre dette for å kompensere for at transaksjonshåndteringen ikke lenger er helt Micronaut-kompatibel..
        // (By default, when using @MicronautTest, each @Test method will be wrapped in a transaction that will be rolled back when the test finishes.)
        txTemplate.doInTransaction { tx ->
            tx.setRollbackOnly()

            val provider = providerRepository.newTestProvider()
            transferLogRepository.save(
                TransferLog(
                    providerId = provider.id!!,
                    md5 = "1a2b3c4d5e",
                    payload = "payload",
                    items = 1
                )
            )
            val date = LocalDateTime.now().plusMonths(7)
            transferLogTasks.deleteTransferLogTask(date)
            assertEquals(0, transferLogRepository.findAll().count())
        }
    }

    @Test
    fun replaceAmpersandsTest() {
        var result = transferLogTasks.sanitizeAd(
            AdDTO(
                adText = "AdText &amp;",
                employer = EmployerDTO(
                    businessName = "BusinessName &amp;",
                    location = LocationDTO(),
                    orgnr = "orgnr &amp;",
                    reference = "Reference &amp;"
                ),
                expires = LocalDateTime.now().plusMonths(1),
                published = LocalDateTime.now(),
                reference = "Reference &amp;",
                title = "Title &amp;"
            )
        )
        assertEquals("Title &", result.title)
        assertEquals("BusinessName &", result.employer?.businessName)
        assertEquals("AdText &amp;", result.adText) // HTML felt skal ikke endres
    }

    @Test
    fun replaceAmpersandsHandleNullValues() {
        var result = transferLogTasks.sanitizeAd(
            AdDTO(
                adText = "?",
                employer = EmployerDTO(null, "test", null, LocationDTO()),
                expires = null,
                published = null,
                reference = "?",
                title = "?",
                locationList = listOf(LocationDTO())
            )
        )
        assertNotNull(result)
    }

    /**
     * TestConfig inneholder en mock på lokalOntologiGateway og responsen med Styrk og Esco som assertes her
     */
    @Test
    fun addStyrkAndEscoToCategoryList() {
        val janzzKonseptId = 123L

        var result = transferLogTasks.sanitizeAd(
            AdDTO(
                adText = "?",
                employer = EmployerDTO(null, "test", null, LocationDTO()),
                expires = null,
                published = null,
                reference = "?",
                title = "?",
                locationList = listOf(LocationDTO()),
                categoryList = listOf(CategoryDTO(code = janzzKonseptId.toString(), name = "Janzzname"))
            )
        )

        assertEquals(3, result.categoryList.size)
        assertEquals(
            janzzKonseptId.toString(),
            result.categoryList.find { cat -> cat.categoryType == CategoryType.JANZZ }?.code
        )
        assertEquals("Janzzname", result.categoryList.find { cat -> cat.categoryType == CategoryType.JANZZ }?.name)
        assertEquals(
            "Spesialsykepleiere",
            result.categoryList.find { cat -> cat.categoryType == CategoryType.STYRK08 }?.name
        )
        assertEquals("2221", result.categoryList.find { cat -> cat.categoryType == CategoryType.STYRK08 }?.code)
        assertEquals(
            "escolabelForKonseptId=$janzzKonseptId",
            result.categoryList.find { cat -> cat.categoryType == CategoryType.ESCO }?.name
        )
        assertEquals(
            "escouriForKonseptId=$janzzKonseptId",
            result.categoryList.find { cat -> cat.categoryType == CategoryType.ESCO }?.code
        )
    }
}
