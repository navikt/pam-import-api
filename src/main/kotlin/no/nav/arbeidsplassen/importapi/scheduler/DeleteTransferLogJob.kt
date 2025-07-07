package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.transferlog.TransferLogScheduler
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

class DeleteTransferLogJob(
    private val transferLogTasks: TransferLogScheduler
) : Job {

    companion object {
        private val LOG = LoggerFactory.getLogger(DeleteTransferLogJob::class.java)
    }

    override fun execute(context: JobExecutionContext?) {
        LOG.info("Executing DeleteTransferLogJob")
        transferLogTasks.startDeleteTransferLogTask()
    }
}
