package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.context.annotation.Requires
import io.micronaut.transaction.annotation.TransactionalEventListener
import no.nav.arbeidsplassen.importapi.Open
import no.nav.arbeidsplassen.importapi.adadminstatus.*
import no.nav.arbeidsplassen.importapi.adstate.AdState
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Singleton

/**
 * This class is only active in LABS-GCP
 */
@Singleton
@Requires(property = "NAIS_CLUSTER_NAME", value="labs-gcp")
@Open
class SyncAdminStatusForLabs(private val adminStatusRepository: AdminStatusRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SyncAdminStatusForLabs.javaClass)
    }

    @PostConstruct
    fun info() {
        LOG.info("We are in running in labs-gcp, it means syncAdminstatus is simulated")
    }

    fun syncAdStateToAdminStatus(adStates: Iterable<AdState>) {
        val adminstatusList = adminStatusRepository.saveAll(adStates.map { it.toAdminSyncStatus() })
        LOG.info("Simulating {} adminstatus to DONE for this adstate," +
                "since we are in labs", adminstatusList.count())
    }

    fun AdState.toAdminSyncStatus(): AdminStatus {
        return adminStatusRepository.findByUuid(uuid)
                .map { it.copy(status = Status.DONE, versionId = versionId) }
                .orElseGet{ AdminStatus(uuid = uuid, status = Status.DONE, versionId = versionId,
                        providerId = providerId,
                        reference = reference) }
    }

    @TransactionalEventListener
    fun onNewAdEvent(event: TransferLogTasks.AdStateEvent) {
        syncAdStateToAdminStatus(event.adList)
    }
}


