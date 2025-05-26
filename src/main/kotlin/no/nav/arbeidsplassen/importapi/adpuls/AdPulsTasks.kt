package no.nav.arbeidsplassen.importapi.adpuls

import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.leaderelection.LeaderElection
import org.slf4j.LoggerFactory

class AdPulsTasks(
    private val adPulsRepository: AdPulsRepository,
    private val leaderElection: LeaderElection
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsTasks::class.java)
    }

    fun deleteOldAdPulsEvents(before: LocalDateTime): Long {
        LOG.info("starting delete old puls events before $before")
        return adPulsRepository.deleteByUpdatedBefore(before)
    }

    fun startDeleteOldPulsEventsTask(): Long {
        if (leaderElection.isLeader()) {
            val count = deleteOldAdPulsEvents(LocalDateTime.now().minusMonths(3))
            LOG.info("$count was deleted")
            return count
        }
        return 0
    }
}
