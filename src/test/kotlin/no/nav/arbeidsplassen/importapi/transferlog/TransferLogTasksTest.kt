package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.common.toMD5Hex
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferJsonString
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryType
import no.nav.arbeidsplassen.importapi.dto.EmployerDTO
import no.nav.arbeidsplassen.importapi.dto.LocationDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogTasksTest : TestRunningApplication() {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(TransferLogTasksTest::class.java)
    }

    private val transferLogTasks: TransferLogTasks = appCtx.servicesApplicationContext.transferLogTasks
    private val transferLogRepository: TransferLogRepository = appCtx.databaseApplicationContext.transferLogRepository
    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val adStateRepository: AdStateRepository = appCtx.databaseApplicationContext.adStateRepository
    private val adOutboxRepository: AdOutboxRepository = appCtx.databaseApplicationContext.adOutboxRepository
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    @Test
    fun doTransferLogTaskTest() {
        txTemplate.doInTransactionNullable { ctx ->

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
            adstates.forEach { LOG.info(it.jsonPayload) }
            assertEquals(2, adstates.count())
            val transferLog =
                transferLogRepository.findByStatus(TransferLogStatus.DONE)
            assertEquals(1, transferLog.count())
            val adOutbox = adOutboxRepository.hentUprosesserteMeldinger(outboxDelay = 0)
            assertEquals(2, adOutbox.size)

            ctx.setRollbackOnly()
        }
    }

    @Test
    fun deleteTransferLogTaskTest() {
        txTemplate.doInTransactionNullable { ctx ->

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

            ctx.setRollbackOnly()
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
