package no.nav.arbeidsplassen.importapi.leaderelection

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.InetAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

class NaisLeaderElection(
    val httpClient: HttpClient,
    val electorPath: String,
    val objectMapper: ObjectMapper
) : LeaderElection {
    private val hostname = InetAddress.getLocalHost().hostName
    private var leader = ""
    private var lastCalled = LocalDateTime.MIN
    private val electorUri = "http://" + electorPath

    companion object {
        private val LOG = LoggerFactory.getLogger(NaisLeaderElection::class.java)
    }

    override fun isLeader(): Boolean {
        return hostname == getLeader()
    }

    private fun getLeader(): String {
        if (electorPath == "NOLEADERELECTION") return hostname
        if (leader.isBlank() || lastCalled.isBefore(LocalDateTime.now().minusMinutes(2))) {
            leader = objectMapper.readValue(getResource(electorUri), Elector::class.java).name
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
