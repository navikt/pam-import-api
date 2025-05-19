package no.nav.arbeidsplassen.importapi.scheduler

import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder.newJob
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MyScheduler(
    private val adOutboxJobEnabled: Boolean = true // @Requires(property = "adoutbox.scheduler.enabled", value = "true")
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MyScheduler::class.java)
    }


    private val adOutboxJob = newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("adOutboxJob", "adOutbox")
        .build()

    // @Scheduled(cron = "*/15 * * * * *")
    private val adOutboxTrigger = newTrigger()
        .withIdentity("adOutboxTrigger", "adOutbox")
        .withSchedule(CronScheduleBuilder.cronSchedule("*/15 * * * * *"))
        .build()


    private val adPulsJob = newJob().ofType(AdOutboxJob::class.java)
        .withIdentity("adPulsJob", "adPuls")
        .build()

    // @Scheduled(cron="05 15 01 * * *")
    private val adPulsTrigger = newTrigger()
        .withIdentity("adPulsTrigger", "adPuls")
        .withSchedule(CronScheduleBuilder.cronSchedule("05 15 01 * * *"))
        .build()

    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()!!

    init {
        if (adOutboxJobEnabled) {
            scheduler.scheduleJob(adOutboxJob, adOutboxTrigger)
        }
        scheduler.scheduleJob(adPulsJob, adPulsTrigger)
    }

    fun start() {
        log.info("Starter scheduler")
        scheduler.start()
        // log.info("Trigger jobb ved oppstart")
        // scheduler.triggerJob(adOutboxJob.key)
    }

    fun stop() {
        log.info("Stopper scheduler")
        scheduler.shutdown()
    }
}
