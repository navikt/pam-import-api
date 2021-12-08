package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
@Open
class AdPulsTasks(private val adPulsRepository: AdPulsRepository, private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsTasks::class.java)
    }
    fun deleteOldAdPulsEvents(before: LocalDateTime): Long {
        LOG.info("starting delete old puls events before $before")
        return adPulsRepository.deleteByUpdatedBefore(before)
    }

    @Scheduled(cron="05 15 01 * * *")
    fun startDeleteOldPulsEventsTask(): Long {
        if (leaderElection.isLeader()) {
            val count = deleteOldAdPulsEvents(LocalDateTime.now().minusMonths(3))
            LOG.info("$count was deleted")
            return count
        }
        return 0
    }
}
