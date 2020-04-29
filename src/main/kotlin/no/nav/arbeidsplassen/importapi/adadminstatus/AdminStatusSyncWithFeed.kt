package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.context.annotation.Value
import no.nav.arbeidsplassen.importapi.feed.AdTransport
import no.nav.arbeidsplassen.importapi.feed.FeedConnector
import no.nav.arbeidsplassen.importapi.feed.Feedtask
import no.nav.arbeidsplassen.importapi.feed.FeedtaskRepository
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.stream.Collectors
import javax.inject.Singleton

@Singleton
class AdminStatusSyncWithFeed(private val feedConnector: FeedConnector,
                              private val feedtaskRepository: FeedtaskRepository,
                              private val adminStatusRepository: AdminStatusRepository,
                              private val meterRegistry: MeterRegistry,
                              @Value("\${adminstatussync.feedurl}") private val feedUrl: String = "http://localhost:9001/api/v1/ads/feed?source=IMPORTAPI") {


    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusSyncWithFeed::class.java)
        private val ADMINSTATUSSYNC_TASK = "AdminStatusSyncTask"
        private val _providerid = "_providerid"
        private val _versionid = "_versionid"
    }

    fun syncAdminStatus() {
        val feedtask = feedtaskRepository.findByName(ADMINSTATUSSYNC_TASK).orElseGet{
            feedtaskRepository.save(Feedtask(name=ADMINSTATUSSYNC_TASK, lastrun = LocalDateTime.now().minusDays(1)))}
        val adList = feedConnector.fetchContentList(feedUrl, feedtask.lastrun, AdTransport::class.java)
        if (adList.isNotEmpty()) {
            LOG.info("Got ${adList.size} to sync adminstatus ")
            val last = adList.last()
            if (adList.size == 1 && last.updated == feedtask.lastrun) {
                LOG.info("Skipping this because last updated ${last.updated} is equal with last run")
                return
            }
            val adminList = adList.stream()
                    .filter{ "IMPORTAPI" == it.source }
                    .map { it.toAdminStatus() }
                    .collect(Collectors.toList())
            adminStatusRepository.saveAll(adminList)
            meterRegistry.counter("ads_admin_sync").increment(adminList.size.toDouble())
            LOG.info("Saved feed task $ADMINSTATUSSYNC_TASK with lastrun date ${last.updated}")
            feedtaskRepository.update(feedtask.copy(lastrun = last.updated))
        }
    }

    private fun AdTransport.toAdminStatus(): AdminStatus {
        return adminStatusRepository.findByUuid(uuid)
                .map { it.copy(status = mapStatus(), versionId = properties[_versionid]?.toLong()!!,
                        message = mapMessage()) }
                .orElseGet{ AdminStatus(uuid = uuid, status = mapStatus(), versionId =
                properties[_versionid]?.toLong()!!, providerId = properties[_providerid]?.toLong()!!,
                        reference = reference, message = mapMessage()) }
    }

    private fun AdTransport.mapMessage(): String? {
        if ("REJECTED".equals(status)) {
            return administration.remarks.toString()
        }
        return null
    }

    private fun AdTransport.mapStatus(): Status {
        return when (administration.status) {
            "DONE" -> Status.DONE
            "PENDING" -> Status.PENDING
            "RECEIVED" -> Status.RECEIVED
            else -> Status.UNKNOWN
        }
    }
}
