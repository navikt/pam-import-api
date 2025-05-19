package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxScheduler
import org.quartz.Job
import org.quartz.JobExecutionContext

class AdOutboxJob(private val adOutboxScheduler: AdOutboxScheduler) : Job {
    override fun execute(context: JobExecutionContext) {
        adOutboxScheduler.startTransferLogTask()
    }
}
