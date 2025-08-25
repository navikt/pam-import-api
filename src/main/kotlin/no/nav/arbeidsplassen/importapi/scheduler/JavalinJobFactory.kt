package no.nav.arbeidsplassen.importapi.scheduler

import kotlin.reflect.jvm.jvmName
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxScheduler
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsTasks
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogScheduler
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

class JavalinJobFactory(
    private val adOutboxScheduler: AdOutboxScheduler,
    private val adPulsTasks: AdPulsTasks,
    private val transferLogScheduler: TransferLogScheduler,
) : JobFactory {

    override fun newJob(bundle: TriggerFiredBundle?, scheduler: Scheduler?): Job? {
        if (bundle != null) {
            val jobClass = bundle.jobDetail.jobClass
            if (jobClass.name == AdOutboxJob::class.jvmName) {
                return AdOutboxJob(adOutboxScheduler)
            }
            if (jobClass.name == AdPulsJob::class.jvmName) {
                return AdPulsJob(adPulsTasks)
            }
            if (jobClass.name == DeleteTransferLogJob::class.jvmName) {
                return DeleteTransferLogJob(
                    transferLogTasks = transferLogScheduler
                )
            }
            if (jobClass.name == TransferLogJob::class.jvmName) {
                return TransferLogJob(transferLogScheduler)
            }
        }
        throw NotImplementedError("Job Factory error")
    }
}
