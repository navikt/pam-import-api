package no.nav.arbeidsplassen.importapi.adoutbox


import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory
import jakarta.inject.Singleton

@Requires(property = "adoutbox.scheduler.enabled", value = "true")
@Singleton
@Open
class AdOutboxScheduler(private val adOutboxService: AdOutboxService, private val leaderElection: LeaderElection) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AdOutboxScheduler::class.java)
    }

    @Scheduled(cron = "*/15 * * * * *")
    fun startTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("Prosesserer AdOutbox-meldinger")
            adOutboxService.prosesserAdOutboxMeldinger()
        }
    }
}
