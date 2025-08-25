package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.adpuls.AdPulsTasks
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

class AdPulsJob(private val adPulsTasks: AdPulsTasks) : Job {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsJob::class.java)
    }

    override fun execute(context: JobExecutionContext?) {
        LOG.info("Executing AdPulsJob")
        adPulsTasks.startDeleteOldPulsEventsTask()
    }
}
