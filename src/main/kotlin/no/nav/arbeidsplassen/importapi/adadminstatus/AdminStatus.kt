package no.nav.arbeidsplassen.importapi.adadminstatus

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
data class AdminStatus(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val uuid: String,
        @Enumerated(EnumType.STRING)
        val status: Status = Status.RECEIVED,
        val message: String? = null,
        val reference: String,
        val providerId: Long,
        val versionId: Long,
        val created: LocalDateTime = LocalDateTime.now(),
        val updated: LocalDateTime = LocalDateTime.now(),
        @Enumerated(EnumType.STRING)
        val publishStatus: PublishStatus = PublishStatus.INACTIVE
)

enum class PublishStatus {
        INACTIVE, ACTIVE, REJECTED, STOPPED, DELETED, UNKNOWN
}

enum class Status {
    RECEIVED, PENDING, DONE, UNKNOWN
}

fun AdminStatus.isNew(): Boolean = id==null
