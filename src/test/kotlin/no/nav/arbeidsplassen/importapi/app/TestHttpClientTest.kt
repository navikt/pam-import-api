package no.nav.arbeidsplassen.importapi.app

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestHttpClientTest {
    @Test
    fun `sends JSON and bearer token relative to the application context path`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":1}"""))
            val client = TestHttpClient(server.url("/stillingsimport/").toString(), jacksonObjectMapper())

            val response = client.post("internal/providers", mapOf("identifier" to "test-provider"), "test-token")

            assertEquals(201, response.statusCode())
            assertEquals("""{"id":1}""", response.body())
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/stillingsimport/internal/providers", request.path)
            assertEquals("Bearer test-token", request.getHeader("Authorization"))
            assertEquals("application/json", request.getHeader("Content-Type"))
            assertEquals("application/json", request.getHeader("Accept"))
            assertEquals("""{"identifier":"test-provider"}""", request.body.readUtf8())
        }
    }

    @Test
    fun `sends raw JSON unchanged and returns error responses for assertions`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"message":"Invalid request"}"""))
            val client = TestHttpClient(server.url("/stillingsimport/").toString(), jacksonObjectMapper())
            val json = """{"ads":[]}"""

            val response = client.post("api/v1/transfers", json, "test-token")

            assertEquals(json, server.takeRequest().body.readUtf8())
            assertEquals(400, response.statusCode())
            assertEquals("""{"message":"Invalid request"}""", response.body())
        }
    }

    @Test
    fun `sends JSON streams with the streaming media type`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":"RECEIVED"}"""))
            val client = TestHttpClient(server.url("/stillingsimport/").toString(), jacksonObjectMapper())
            val stream = """{"reference":"first"}{"reference":"second"}"""

            val response = client.postJsonStream("api/v1/transfers/1", stream, "test-token")

            assertEquals(200, response.statusCode())
            val request = server.takeRequest()
            assertEquals("application/x-json-stream", request.getHeader("Content-Type"))
            assertEquals("application/x-json-stream", request.getHeader("Accept"))
            assertEquals(stream, request.body.readUtf8())
        }
    }
}
