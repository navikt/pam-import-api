package no.nav.arbeidsplassen.importapi.transferlog


import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory

@Requires(property = "transferlog.scheduler.enabled", value = "true")
@Singleton
@Open
class TransferLogScheduler(private val transferLogTasks: TransferLogTasks, private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogScheduler::class.java)
    }

    @Scheduled(cron = "*/30 * * * * *")
    fun startTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("Running transferLogTask")
            val count = transferLogTasks.processTransferLogTask()
            LOG.info("Processed $count transferLogTasks")
        }
    }

    @Scheduled(cron = "05 15 00 * * *")
    fun startDeleteTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("starting deletTransferLogTask")
            transferLogTasks.deleteTransferLogTask()
        }
    }
}
