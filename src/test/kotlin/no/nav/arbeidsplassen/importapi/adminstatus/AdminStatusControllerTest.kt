package no.nav.arbeidsplassen.importapi.adminstatus

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import java.net.URI
import java.util.UUID
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.PublishStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.Status
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.findTestProvider
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.security.TokenService
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminStatusControllerTest : TestRunningApplication() {

    private val tokenService: TokenService = appCtx.securityServicesApplicationContext.tokenService
    private val objectMapper: ObjectMapper = appCtx.baseServicesApplicationContext.objectMapper

    private val client: Rx3HttpClient = Rx3HttpClient.create(URI(lokalUrlBase).toURL())

    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusControllerTest::class.java)
        private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
        private val transferLogRepository: TransferLogRepository =
            appCtx.databaseApplicationContext.transferLogRepository
        private val adminStatusRepository: AdminStatusRepository =
            appCtx.databaseApplicationContext.adminStatusRepository

        @BeforeAll
        @JvmStatic
        fun setup() {
            val provider = providerRepository.newTestProvider()
            val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring", items = 1)
            val transferInDb = transferLogRepository.save(transferLog)
            val adminStatus = AdminStatus(
                reference = "12345", providerId = provider.id!!, status = Status.DONE,
                versionId = transferInDb.id!!, uuid = UUID.randomUUID().toString(), publishStatus = PublishStatus.ACTIVE
            )
            adminStatusRepository.save(adminStatus)
            val read =
                adminStatusRepository.findByProviderIdAndReference(providerId = provider.id!!, reference = "12345")
            Assertions.assertNotNull(read)
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            val providerId = providerRepository.findTestProvider().id!!
            transferLogRepository.deleteByProviderId(providerId)
            adminStatusRepository.deleteByProviderId(providerId)
            providerRepository.deleteById(providerId)
        }
    }


    @Test
    fun `get adminstatus receipts`() {
        val providerId = providerRepository.findTestProvider().id!!

        val adminToken = tokenService.adminToken()
        val get = HttpRequest.GET<AdAdminStatusDTO>("api/v1/adminstatus/$providerId/12345")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val adminstatus = client.exchange(get, AdAdminStatusDTO::class.java).blockingFirst()
        assertEquals(adminstatus.body.get().reference, "12345")
        LOG.info(objectMapper.writeValueAsString(adminstatus.body.get()))
    }
}
