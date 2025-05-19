package no.nav.arbeidsplassen.importapi.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDateTime

// env ELECTOR_PATH eller "NOLEADERELECTION"
class LeaderElector(private val electorPath: String, private val httpClient: HttpClient) {
    private val hostname = InetAddress.getLocalHost().hostName
    private var leader =  ""
    private var lastCalled = LocalDateTime.MIN
    private val electorUri = "http://"+electorPath

    companion object {
        private val LOG = LoggerFactory.getLogger(LeaderElector::class.java)
        private val jacksonMapper = jacksonObjectMapper()
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(JavaTimeModule())

    }

    fun isLeader(): Boolean {
        return hostname == getLeader()
    }

    private fun getLeader(): String {
        if (electorPath == "NOLEADERELECTION") return hostname
        if (leader.isBlank() || lastCalled.isBefore(LocalDateTime.now().minusMinutes(2))) {
            leader = jacksonMapper.readValue(getResource(electorUri), Elector::class.java).name
            LOG.debug("Running leader election getLeader is {} ", leader)
            lastCalled = LocalDateTime.now()
        }
        return leader
    }

    private fun getResource(uri: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI(uri))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build()

        val response = httpClient
            .send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() >= 300 || response.body() == null) {
            LOG.error("Greide ikke å hente leader fra $uri ${response.statusCode()} : ${response.body()}")
            throw RuntimeException("unknown error (responseCode=${response.statusCode()}) ved henting av leader")
        }

        return response.body()
    }
}

data class Elector(val name: String)
