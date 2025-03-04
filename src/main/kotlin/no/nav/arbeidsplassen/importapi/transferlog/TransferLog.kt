package no.nav.arbeidsplassen.importapi.transferlog

import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.repository.Entity

data class TransferLog(
    override var id: Long? = null,
    val providerId: Long,
    val md5: String,
    val items: Int,
    val payload: String,
    val status: TransferLogStatus = TransferLogStatus.RECEIVED,
    val message: String? = null,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now()
) : Entity

enum class TransferLogStatus {
    RECEIVED, DONE, ERROR, SKIPPED
}
