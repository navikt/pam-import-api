package no.nav.arbeidsplassen.importapi.adadminstatus

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
class AdminStatusSync(private val feedConnector: FeedConnector,
                      private val feedtaskRepository: FeedtaskRepository,
                      private val adminStatusRepository: AdminStatusRepository,
                      @Value("\${adminstatussync.feedurl}") private val feedUrl: String = "http://localhost:9001/api/v1/ads/feed") {

    private val ADMINSTATUSSYNC_TASK = "AdminStatusSyncTask"

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusSync::class.java)
    }

    fun syncAdminStatus() {
        val feedtask = feedtaskRepository.findByName(ADMINSTATUSSYNC_TASK).orElseGet{
            feedtaskRepository.save(Feedtask(name=ADMINSTATUSSYNC_TASK, lastrun = LocalDateTime.now()))}
        val adList = feedConnector.fetchContentList(feedUrl, feedtask.lastrun, AdTransport::class.java)
        if (adList.isNotEmpty()) {
            val last = adList.last()
            val adminList = adList.stream()
                    .filter{ "IMPORTAPI" == it.source }
                    .map { it.toAdminStatus() }
                    .collect(Collectors.toList())
            adminStatusRepository.saveAll(adminList)
            LOG.info("Saved feed task $ADMINSTATUSSYNC_TASK with lastrun date ${last.updated}")
            feedtaskRepository.update(feedtask.copy(lastrun = last.updated))
        }
    }

    private fun AdTransport.toAdminStatus(): AdminStatus {
        return adminStatusRepository.findByUuid(uuid)
                .map { it.copy(status = mapStatus(), versionId = properties["_versionid"]?.toLong()!!,
                        message = mapMessage()) }
                .orElseGet{ AdminStatus(uuid = uuid, status = mapStatus(), versionId =
                properties["_versionid"]?.toLong()!!, providerId = properties["_propertyid"]?.toLong()!!,
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
