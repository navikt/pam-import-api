package no.nav.arbeidsplassen.importapi.adadminstatus

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
data class AdAdminStatus(
        @Id
        @GeneratedValue
        var id: Long? = null,
        val uuid: UUID = UUID.randomUUID(),
        @Enumerated(EnumType.STRING)
        val status: Status = Status.RECEIVED,
        val message: String? = null,
        val reference: String,
        val providerId: Long,
        val created: LocalDateTime = LocalDateTime.now(),
        val updated: LocalDateTime = LocalDateTime.now()
)

enum class Status {
    RECEIVED, ACTIVE, STOPPED, REJECTED
}

fun AdAdminStatus.isNew(): Boolean = id==null
