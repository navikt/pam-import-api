package no.nav.arbeidsplassen.importapi.scheduler

import no.nav.arbeidsplassen.importapi.adpuls.AdPulsTasks
import org.quartz.Job
import org.quartz.JobExecutionContext

class AdPulsJob(private val adPulsTasks: AdPulsTasks) : Job {
    override fun execute(context: JobExecutionContext?) {
        adPulsTasks.startDeleteOldPulsEventsTask()
    }
}
