package no.nav.arbeidsplassen.importapi.transferlog

import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class TransferLog (
        @Id
        @GeneratedValue
        var id: Long? = null,
        val providerId: Long,
        val md5: String,
        val items: Int,
        val payload: String,
        @Enumerated(EnumType.STRING)
        val status: TransferLogStatus = TransferLogStatus.RECEIVED,
        val message: String? = null,
        val created: LocalDateTime = LocalDateTime.now(),
        val updated: LocalDateTime = LocalDateTime.now()
)

fun TransferLog.isNew(): Boolean = id == null

enum class TransferLogStatus {
        RECEIVED, DONE, ERROR
}
