package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Value
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class LeaderElection(@Client("LeaderElect") val client: RxHttpClient,
                     @Value("\${ELECTOR_PATH:NOLEADERELECTION}") val electorPath: String) {

    private val hostname = InetAddress.getLocalHost().hostName
    private var leader =  "";
    private var lastCalled = LocalDateTime.MIN


    companion object {
        private val LOG = LoggerFactory.getLogger(LeaderElection::class.java)
    }

    fun isLeader(): Boolean {
        return hostname == getLeader();
    }

    private fun getLeader(): String {
        if (electorPath == "NOLEADERELECTION") return hostname;
        if (leader.isBlank() || lastCalled.isBefore(LocalDateTime.now().minusMinutes(2))) {
            leader = client.exchange(electorPath,Elector::class.java).blockingFirst().body()!!.name
            LOG.debug("Running leader election getLeader is {} ", leader)
            lastCalled = LocalDateTime.now()
        }
        return leader
    }
}

data class Elector(val name: String)
