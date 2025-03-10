package no.nav.arbeidsplassen.importapi.adpuls

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import java.time.LocalDateTime
import java.util.UUID
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
@Property(name = "JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class AdPulsControllerTest(
    private val objectMapper: ObjectMapper,
    private val tokenService: TokenService,
    private val repository: AdPulsRepository,
    private val providerRepository: ProviderRepository
) {

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var client: Rx3HttpClient

    @Test
    fun `create provider and then upload ads in batches`() {

        val provider = providerRepository.newTestProvider()
        val first = repository.save(
            AdPuls(
                providerId = provider.id!!,
                uuid = UUID.randomUUID().toString(),
                reference = UUID.randomUUID().toString(),
                type = PulsEventType.pageviews,
                total = 10
            )
        )
        val inDb = repository.findById(first.id!!)!!
        val new = inDb.copy(total = 20)
        repository.save(new)

        val providerId = provider.id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val getRequest =
            HttpRequest.GET<String>("/api/v1/stats/${providerId}?from=${from}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)

        val response = client.exchange(getRequest, TransferLogDTO::class.java).blockingFirst().body()
        assertEquals(HttpStatus.OK, response.status)
    }

}
