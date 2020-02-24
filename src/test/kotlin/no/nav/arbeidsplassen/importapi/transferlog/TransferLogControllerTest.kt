package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.transferJsonString
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class TransferLogControllerTest(private val objectMapper: ObjectMapper) {

    @Inject
    @field:Client("/")
    lateinit var client: RxHttpClient

    @Test
    fun postAdTransfer() {
        val post  = HttpRequest.POST("/api/v1/transfers", objectMapper.transferJsonString())
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        val response = client.exchange(post, String::class.java).blockingFirst()
        val request = HttpRequest.GET<String>("/internal/isAlive")
        val body = client.toBlocking().retrieve(request)
        println(body)
    }

}