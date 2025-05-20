package no.nav.arbeidsplassen.importapi.transferlog


import no.nav.arbeidsplassen.importapi.LeaderElection
import org.slf4j.LoggerFactory

class TransferLogScheduler(private val transferLogTasks: TransferLogTasks, private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogScheduler::class.java)
    }

    fun startTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("Running transferLogTask")
            val count = transferLogTasks.processTransferLogTask()
            LOG.info("Processed $count transferLogTasks")
        }
    }

    fun startDeleteTransferLogTask() {
        if (leaderElection.isLeader()) {
            LOG.info("starting deletTransferLogTask")
            transferLogTasks.deleteTransferLogTask()
        }
    }
}
