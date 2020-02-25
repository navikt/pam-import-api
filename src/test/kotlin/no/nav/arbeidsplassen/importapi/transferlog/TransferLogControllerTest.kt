package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class TransferLogControllerTest(private val objectMapper: ObjectMapper) {

    @Inject
    @field:Client("/")
    lateinit var client: RxHttpClient

    @Test
    fun postAdTransfer() {
        val postProvider = HttpRequest.POST("/internal/providers", ProviderDTO(identifier = "webcruiter", email = "test@test.no"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        val message = client.exchange(postProvider, ProviderDTO::class.java).blockingFirst()
        assertEquals(HttpStatus.CREATED, message.status)
        val provider = message.body()
        val post  = HttpRequest.POST("/api/v1/transfers/${provider?.id}", objectMapper.transferToAdList())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        val response = client.exchange(post, TransferLogDTO::class.java).blockingFirst()
        assertEquals(HttpStatus.CREATED, response.status)
        println(response.body())
    }

}