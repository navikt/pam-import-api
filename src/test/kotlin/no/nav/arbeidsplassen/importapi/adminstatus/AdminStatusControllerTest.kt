package no.nav.arbeidsplassen.importapi.adminstatus

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatus
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.Status
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.AdAdminStatusDTO
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.security.TokenService
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject


@MicronautTest
@Property(name="JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class AdminStatusControllerTest(private val tokenService: TokenService,
                                private val providerRepository: ProviderRepository,
                                private val transferLogRepository: TransferLogRepository,
                                private val adminStatusRepository: AdminStatusRepository,
                                private val objectMapper: ObjectMapper) {

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var client: RxHttpClient

    @BeforeEach
    fun createAdminStatus() {
        val provider = providerRepository.newTestProvider()
        val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring", items = 1)
        val transferInDb = transferLogRepository.save(transferLog)
        val adminStatus = AdminStatus(reference = "12345", providerId = provider.id!!, status = Status.DONE,
                versionId = transferInDb.id!!, uuid = UUID.randomUUID().toString())
        adminStatusRepository.save(adminStatus)
        val read = adminStatusRepository.findByProviderIdAndReference(providerId = provider.id!!, reference = "12345")
        Assertions.assertNotNull(read)
    }

    @Test
    fun `get adminstatus receipts`() {
        val adminToken = tokenService.adminToken()
        val get  = HttpRequest.GET<AdAdminStatusDTO>("/api/v1/adminstatus/10000/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)
        val adminstatus = client.exchange(get, AdAdminStatusDTO::class.java).blockingFirst()
        assertEquals(adminstatus.body.get().reference, "12345")
        println(objectMapper.writeValueAsString(adminstatus.body.get()))
    }
}