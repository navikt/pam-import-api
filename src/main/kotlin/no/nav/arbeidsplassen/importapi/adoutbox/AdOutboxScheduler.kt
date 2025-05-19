package no.nav.arbeidsplassen.importapi.adoutbox


import no.nav.arbeidsplassen.importapi.LeaderElection
import org.slf4j.LoggerFactory

class AdOutboxScheduler(private val adOutboxService: AdOutboxService, private val leaderElection: LeaderElection) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AdOutboxScheduler::class.java)
    }

    fun startTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("Prosesserer AdOutbox-meldinger")
            adOutboxService.prosesserAdOutboxMeldinger()
        }
    }
}
