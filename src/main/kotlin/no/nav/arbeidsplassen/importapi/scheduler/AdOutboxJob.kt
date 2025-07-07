package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxScheduler
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

class AdOutboxJob(private val adOutboxScheduler: AdOutboxScheduler) : Job {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdOutboxJob::class.java)
    }

    override fun execute(context: JobExecutionContext) {
        LOG.info("Executing AdOutboxJob")
        adOutboxScheduler.startTransferLogTask()
    }
}
