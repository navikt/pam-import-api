package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpRequest.PUT
import io.micronaut.http.MediaType
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import java.net.URI
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProviderControllerTest : TestRunningApplication() {

    private val tokenService: TokenService = appCtx.securityServicesApplicationContext.tokenService
    private val client: Rx3HttpClient = Rx3HttpClient.create(URI(lokalUrlBase).toURL())

    @Test
    fun `create read update provider`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val create = POST(
            "internal/providers",
            ProviderDTO(identifier = "webcruiter", email = "test@test.no", phone = "12345678")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val created = client.exchange(create, ProviderDTO::class.java).blockingFirst().body()
        val read = GET<Long>("internal/providers/${created?.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val reddit = client.exchange(read, ProviderDTO::class.java).blockingFirst().body()
        assertEquals(created, reddit)
        val put = PUT(
            "internal/providers/${created?.id}",
            ProviderDTO(identifier = "webcruiter2", email = "test@test.no", phone = "12345678")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        client.exchange(put, ProviderDTO::class.java).blockingFirst()
        val updated = client.exchange(read, ProviderDTO::class.java).blockingFirst().body()
        assertEquals("webcruiter2", updated?.identifier)

    }
}
