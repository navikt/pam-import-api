package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Around
@Singleton
@Requires(property = "transferlog.scheduler.enabled", value = "true")
class TransferLogScheduler(private val transferLogTasks: TransferLogTasks) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogScheduler::class.java)
    }
    @Scheduled(cron="0,10,20,30,40,50 * * * * *")
    @SchedulerLock(name = "doTransferLogTask")
    fun startTransferLogTask() {
        LOG.info("starting transferlogtask")
        transferLogTasks.doTransferLogTask()
    }

    @Scheduled(cron="05 15 00 * * * ")
    @SchedulerLock(name="deleteTransferLogTask")
    fun startDeleteTransferLogTask() {
        LOG.info("starting deletTransferLogTask")
        transferLogTasks.deleteTransferLogTask()
    }

}