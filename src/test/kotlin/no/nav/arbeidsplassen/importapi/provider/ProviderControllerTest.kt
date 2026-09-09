package no.nav.arbeidsplassen.importapi.provider

import no.nav.arbeidsplassen.importapi.app.TestHttpClient
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProviderControllerTest : TestRunningApplication() {

    private val tokenService: TokenService = appCtx.securityServicesApplicationContext.tokenService
    private val objectMapper = appCtx.baseServicesApplicationContext.objectMapper
    private val client = TestHttpClient(lokalUrlBase, objectMapper)

    @Test
    fun `create read update provider`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val create = client.post(
            "internal/providers",
            ProviderDTO(identifier = "webcruiter", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, create.statusCode())
        val created = objectMapper.readValue(create.body(), ProviderDTO::class.java)
        val path = "internal/providers/${created.id}"
        val read = client.get(path, adminToken)
        assertEquals(200, read.statusCode())
        val reddit = objectMapper.readValue(read.body(), ProviderDTO::class.java)
        assertEquals(created, reddit)
        val put = client.put(
            path,
            ProviderDTO(identifier = "webcruiter2", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(200, put.statusCode())
        val updatedResponse = client.get(path, adminToken)
        assertEquals(200, updatedResponse.statusCode())
        val updated = objectMapper.readValue(updatedResponse.body(), ProviderDTO::class.java)
        assertEquals("webcruiter2", updated.identifier)

    }
}
