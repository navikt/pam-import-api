package no.nav.arbeidsplassen.importapi.adadminstatus

import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.Entity

data class AdminStatus(
    override var id: Long? = null,
    val uuid: String,
    val status: Status = Status.RECEIVED,
    val message: String? = null,
    val reference: String,
    val providerId: Long,
    val versionId: Long,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val publishStatus: PublishStatus = PublishStatus.INACTIVE
) : Entity

enum class PublishStatus {
    INACTIVE, ACTIVE, REJECTED, STOPPED, DELETED, UNKNOWN
}

enum class Status {
    RECEIVED, PENDING, DONE, UNKNOWN
}
