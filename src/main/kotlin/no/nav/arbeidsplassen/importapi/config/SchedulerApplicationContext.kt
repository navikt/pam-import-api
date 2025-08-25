package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.scheduler.AdOutboxJob
import no.nav.arbeidsplassen.importapi.scheduler.AdPulsJob
import no.nav.arbeidsplassen.importapi.scheduler.DeleteTransferLogJob
import no.nav.arbeidsplassen.importapi.scheduler.JavalinJobFactory
import no.nav.arbeidsplassen.importapi.scheduler.TransferLogJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SchedulerApplicationContext(
    val schedulerConfigProperties: SchedulerConfigProperties,
    servicesApplicationContext: ServicesApplicationContext
) : OnServerStartup, OnServerShutdown {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SchedulerApplicationContext::class.java)
    }

    private val adOutboxJob = JobBuilder.newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("adOutboxJob", "adOutbox")
        .build()

    private val adOutboxTrigger = TriggerBuilder.newTrigger()
        .withIdentity("adOutboxTrigger", "adOutbox")
        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(15))
        .build()


    private val adPulsJob = JobBuilder.newJob().ofType(AdPulsJob::class.java)
        .withIdentity("adPulsJob", "adPuls")
        .build()

    private val adPulsTrigger = TriggerBuilder.newTrigger()
        .withIdentity("adPulsTrigger", "adPuls")
        .withSchedule(CronScheduleBuilder.cronSchedule("5 15 1 * * ?"))
        .build()

    private val transferLogJob = JobBuilder.newJob().ofType(TransferLogJob::class.java)
        .withIdentity("transferLogJob", "transferLog")
        .build()

    private val transferLogTrigger = TriggerBuilder.newTrigger()
        .withIdentity("transferLogTrigger", "transferLog")
        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(30))
        .build()

    private val deleteTransferLogJob = JobBuilder.newJob().ofType(DeleteTransferLogJob::class.java)
        .withIdentity("deleteTransferLogJob", "deleteTransferLog")
        .build()

    private val deleteTransferLogTrigger = TriggerBuilder.newTrigger()
        .withIdentity("deleteTransferLogTrigger", "deleteTransferLog")
        .withSchedule(CronScheduleBuilder.cronSchedule("5 15 0 * * ?"))
        .build()

    private val jobFactory = JavalinJobFactory(
        adOutboxScheduler = servicesApplicationContext.adOutboxScheduler,
        adPulsTasks = servicesApplicationContext.adPulsTasks,
        transferLogScheduler = servicesApplicationContext.transferLogScheduler,
    )

    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()!!

    init {
        LOG.info("Creating scheduler")
        scheduler.setJobFactory(jobFactory)
        if (schedulerConfigProperties.adOutboxJobEnabled) {
            LOG.info("Scheduling Ad Outbox Job")
            scheduler.scheduleJob(adOutboxJob, adOutboxTrigger)
        } else {
            LOG.info("NOT scheduling Ad Outbox Job")
        }
        scheduler.scheduleJob(adPulsJob, adPulsTrigger)
        if (schedulerConfigProperties.transferlogJobEnabled) {
            LOG.info("Scheduling Transferlog Jobs")
            scheduler.scheduleJob(transferLogJob, transferLogTrigger)
            scheduler.scheduleJob(deleteTransferLogJob, deleteTransferLogTrigger)
        } else {
            LOG.info("NOT scheduling Transferlog Jobs")
        }
    }

    override fun onServerStartup() {
        LOG.info("Starter scheduler")
        scheduler.start()
        if (schedulerConfigProperties.adOutboxJobEnabled) {
            LOG.info("Trigger jobb ved oppstart")
            scheduler.triggerJob(adOutboxJob.key)
        }
    }

    override fun onServerShutdown() {
        LOG.info("Stopper scheduler")
        scheduler.shutdown()
    }
}
