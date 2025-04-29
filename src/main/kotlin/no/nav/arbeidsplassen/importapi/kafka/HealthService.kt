package no.nav.arbeidsplassen.importapi.kafka

import jakarta.inject.Singleton
import java.util.concurrent.atomic.AtomicInteger

@Singleton
class HealthService {
    private val unhealthyVotes = AtomicInteger(0)
    fun addUnhealthyVote() = unhealthyVotes.addAndGet(1)
    fun isHealthy() = unhealthyVotes.get() == 0
}
