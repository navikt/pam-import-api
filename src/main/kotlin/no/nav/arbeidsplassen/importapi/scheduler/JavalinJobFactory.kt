package no.nav.arbeidsplassen.importapi.scheduler

import kotlin.reflect.jvm.jvmName
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxScheduler
import org.quartz.Job
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

class JavalinJobFactory(
    private val adOutboxScheduler: AdOutboxScheduler
) : JobFactory {

    override fun newJob(bundle: TriggerFiredBundle?, scheduler: org.quartz.Scheduler?): Job? {
        if (bundle != null) {
            val jobClass = bundle.jobDetail.jobClass
            if (jobClass.name == AdOutboxJob::class.jvmName) {
                return AdOutboxJob(adOutboxScheduler)
            }
        }
        throw NotImplementedError("Job Factory error")
    }
}
