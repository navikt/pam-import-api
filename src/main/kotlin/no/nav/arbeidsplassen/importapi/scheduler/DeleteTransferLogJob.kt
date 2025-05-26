package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.transferlog.TransferLogScheduler
import org.quartz.Job
import org.quartz.JobExecutionContext

class DeleteTransferLogJob(
    private val transferLogTasks: TransferLogScheduler
) : Job {
    override fun execute(context: JobExecutionContext?) {
        transferLogTasks.startDeleteTransferLogTask()
    }
}
