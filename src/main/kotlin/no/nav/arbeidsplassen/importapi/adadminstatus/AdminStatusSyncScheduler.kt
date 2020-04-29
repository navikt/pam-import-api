package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import net.javacrumbs.shedlock.micronaut.SchedulerLock
import no.nav.arbeidsplassen.importapi.Open
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@Open
@Requires(property = "adminstatussync.scheduler.enabled", value="true")
class AdminStatusSyncScheduler(private val adminStatusSyncWithFeed: AdminStatusSyncWithFeed) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusSyncScheduler::class.java)
    }

    @Scheduled(cron = "30 * * * * *")
    @SchedulerLock(name = "adminStatusSyncTask")
    fun startAdminStatusSyncTask() {
        LOG.info("Starting AdminStatusSync Task")
        adminStatusSyncWithFeed.syncAdminStatus()
    }

}