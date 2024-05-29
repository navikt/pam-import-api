package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest.*
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


@MicronautTest
@Property(name="JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class ProviderControllerTest(private val tokenService: TokenService) {

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var client: Rx3HttpClient

    @Test
    fun `create read update provider`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val create = POST("/internal/providers",
                ProviderDTO(identifier = "webcruiter", email = "test@test.no", phone = "12345678"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)
        val created = client.exchange(create, ProviderDTO::class.java).blockingFirst().body()
        val read = GET<Long>("/internal/providers/${created?.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)
        val reddit = client.exchange(read, ProviderDTO::class.java).blockingFirst().body()
        assertEquals(created, reddit)
        val put = PUT("/internal/providers/${created?.id}",
                ProviderDTO(identifier = "webcruiter2", email = "test@test.no", phone = "12345678"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)
        client.exchange(put, ProviderDTO::class.java).blockingFirst()
        val updated = client.exchange(read, ProviderDTO::class.java).blockingFirst().body()
        assertEquals("webcruiter2", updated?.identifier)

    }
}
