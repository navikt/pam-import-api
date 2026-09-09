package no.nav.arbeidsplassen.importapi.app

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class TestHttpClient(baseUrl: String, private val objectMapper: ObjectMapper) {
    private val baseUri = URI.create(baseUrl)

    fun get(path: String, token: String): HttpResponse<String> = send("GET", path, token)

    fun post(path: String, body: Any, token: String): HttpResponse<String> = send("POST", path, token, body)

    fun postJsonStream(path: String, body: String, token: String): HttpResponse<String> =
        send("POST", path, token, body, "application/x-json-stream")

    fun put(path: String, body: Any, token: String): HttpResponse<String> = send("PUT", path, token, body)

    fun delete(path: String, token: String): HttpResponse<String> = send("DELETE", path, token)

    private fun send(
        method: String,
        path: String,
        token: String,
        body: Any? = null,
        contentType: String = "application/json"
    ): HttpResponse<String> {
        val publisher = when (body) {
            null -> HttpRequest.BodyPublishers.noBody()
            is String -> HttpRequest.BodyPublishers.ofString(body)
            else -> HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))
        }
        val request = HttpRequest.newBuilder(baseUri.resolve(path))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", contentType)
            .header("Accept", contentType)
            .header("Authorization", "Bearer $token")
            .method(method, publisher)
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        private val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build()
    }
}
