package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.scheduler.AdOutboxJob
import no.nav.arbeidsplassen.importapi.scheduler.JavalinJobFactory
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class SchedulerConfigurationProperties(
    val adOutboxJobEnabled: Boolean,
    val transferlogJobEnabled: Boolean,
) {
    companion object {
        fun SchedulerConfigurationProperties(env: Map<String, String>): SchedulerConfigurationProperties =
            SchedulerConfigurationProperties(
                adOutboxJobEnabled = env.nullableVariable("adoutbox.scheduler.enabled")?.toBoolean() ?: false,
                transferlogJobEnabled = env.nullableVariable("transferlog.scheduler.enabled")?.toBoolean() ?: false,
            )
    }
}

class SchedulerApplicationContext(
    schedulerConfigurationProperties: SchedulerConfigurationProperties,
    servicesApplicationContext: ServicesApplicationContext
) : OnServerStartup, OnServerShutdown {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SchedulerApplicationContext::class.java)
    }


    private val adOutboxJob = JobBuilder.newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("adOutboxJob", "adOutbox")
        .build()

    // @Scheduled(cron = "*/15 * * * * *")
    private val adOutboxTrigger = TriggerBuilder.newTrigger()
        .withIdentity("adOutboxTrigger", "adOutbox")
        .withSchedule(CronScheduleBuilder.cronSchedule("*/15 * * * * *"))
        .build()


    private val adPulsJob = JobBuilder.newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("adPulsJob", "adPuls")
        .build()

    // @Scheduled(cron="05 15 01 * * *")
    private val adPulsTrigger = TriggerBuilder.newTrigger()
        .withIdentity("adPulsTrigger", "adPuls")
        .withSchedule(CronScheduleBuilder.cronSchedule("05 15 01 * * *"))
        .build()

    private val transferLogJob = JobBuilder.newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("transferLogJob", "transferLog")
        .build()

    private val transferLogTrigger = TriggerBuilder.newTrigger()
        .withIdentity("transferLogTrigger", "transferLog")
        .withSchedule(CronScheduleBuilder.cronSchedule("*/30 * * * * *"))
        .build()

    private val deleteTransferLogJob = JobBuilder.newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("deleteTransferLogJob", "deleteTransferLog")
        .build()

    private val deleteTransferLogTrigger = TriggerBuilder.newTrigger()
        .withIdentity("deleteTransferLogTrigger", "deleteTransferLog")
        .withSchedule(CronScheduleBuilder.cronSchedule("05 15 00 * * *"))
        .build()

    private val jobFactory = JavalinJobFactory(
        adOutboxScheduler = servicesApplicationContext.adOutboxScheduler,
        adPulsTasks = servicesApplicationContext.adPulsTasks,
        transferLogScheduler = servicesApplicationContext.transferLogScheduler,
    )

    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()!!

    init {
        scheduler.setJobFactory(jobFactory)
        if (schedulerConfigurationProperties.adOutboxJobEnabled) {
            scheduler.scheduleJob(adOutboxJob, adOutboxTrigger)
        }
        scheduler.scheduleJob(adPulsJob, adPulsTrigger)
        if (schedulerConfigurationProperties.transferlogJobEnabled) {
            scheduler.scheduleJob(transferLogJob, transferLogTrigger)
            scheduler.scheduleJob(deleteTransferLogJob, deleteTransferLogTrigger)
        }
    }

    override fun onServerStartup() {
        LOG.info("Starter scheduler")
        scheduler.start()
        LOG.info("Trigger jobb ved oppstart")
        scheduler.triggerJob(adOutboxJob.key)
    }

    override fun onServerShutdown() {
        LOG.info("Stopper scheduler")
        scheduler.shutdown()
    }
}
