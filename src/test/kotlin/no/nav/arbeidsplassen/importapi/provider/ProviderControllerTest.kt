package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpRequest.PUT
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


@MicronautTest
@Property(name = "JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class ProviderControllerTest(private val tokenService: TokenService) {

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var client: Rx3HttpClient

    @Test
    fun `create read update provider`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val create = POST(
            "/internal/providers",
            ProviderDTO(identifier = "webcruiter", email = "test@test.no", phone = "12345678")
        )
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
        val put = PUT(
            "/internal/providers/${created?.id}",
            ProviderDTO(identifier = "webcruiter2", email = "test@test.no", phone = "12345678")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        client.exchange(put, ProviderDTO::class.java).blockingFirst()
        val updated = client.exchange(read, ProviderDTO::class.java).blockingFirst().body()
        assertEquals("webcruiter2", updated?.identifier)
    }

    /*
    // HPH: Jeg beholder dette for å dokumentere hvordan man kan kalle en metode som tar pageable som input
    // og returnerer slice som output.

    @Test
    fun `list providers`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val create1 = POST("/internal/providers",
            ProviderDTO(identifier = "webcruiter1", email = "test1@test.no", phone = "12345678"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val create2 = POST("/internal/providers",
            ProviderDTO(identifier = "webcruiter2", email = "test2@test.no", phone = "12345678"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val create3 = POST("/internal/providers",
            ProviderDTO(identifier = "webcruiter3", email = "test3@test.no", phone = "12345678"))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)

        client.exchange(create1, ProviderDTO::class.java).blockingFirst().body()
        client.exchange(create2, ProviderDTO::class.java).blockingFirst().body()
        client.exchange(create3, ProviderDTO::class.java).blockingFirst().body()

        val bodyType = object: GenericArgument<Slice<ProviderDTO>>() {}
        // Man kan iterere gjennom pageable ved en URL ala /internal/providers?size=200&page=2
        val read = GET<String>("/internal/providers")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val reddit : Slice<ProviderDTO> = client.exchange(read, bodyType).blockingFirst().body()
        assertEquals(3, reddit.content.size)
    }

     */
}
