package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.time.LocalDateTime
import jakarta.inject.Singleton

@Singleton
class LeaderElection(@Client("LeaderElect") val client: RxHttpClient,
                     @Value("\${ELECTOR_PATH:NOLEADERELECTION}") val electorPath: String,
                     val objectMapper: ObjectMapper) {

    private val hostname = InetAddress.getLocalHost().hostName
    private var leader =  "";
    private var lastCalled = LocalDateTime.MIN
    private val electorUri = "http://"+electorPath;

    companion object {
        private val LOG = LoggerFactory.getLogger(LeaderElection::class.java)
    }

    fun isLeader(): Boolean {
        return hostname == getLeader();
    }

    private fun getLeader(): String {
        if (electorPath == "NOLEADERELECTION") return hostname;
        if (leader.isBlank() || lastCalled.isBefore(LocalDateTime.now().minusMinutes(2))) {
            leader = objectMapper.readValue(client.retrieve(electorUri).blockingFirst(), Elector::class.java).name
            LOG.debug("Running leader election getLeader is {} ", leader)
            lastCalled = LocalDateTime.now()
        }
        return leader
    }
}

data class Elector(val name: String)
