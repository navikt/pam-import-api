package no.nav.arbeidsplassen.importapi.transferlog


import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Requires(property = "transferlog.scheduler.enabled", value = "true")
@Singleton
@Open
class TransferLogScheduler(private val transferLogTasks: TransferLogTasks) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogScheduler::class.java)
    }

    @SchedulerLock(name = "doTransferLogTask")
    @Scheduled(cron="*/30 * * * * *")
    @Transactional
    fun startTransferLogTask() {
        LOG.info("starting transferlogtask")
        transferLogTasks.processTransferLogTask()
    }

    @Scheduled(cron="05 15 00 * * *")
    @SchedulerLock(name="deleteTransferLogTask")
    @Transactional
    fun startDeleteTransferLogTask() {
        LOG.info("starting deletTransferLogTask")
        transferLogTasks.deleteTransferLogTask()
    }

}
