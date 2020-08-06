package no.nav.arbeidsplassen.importapi.dto

import no.nav.arbeidsplassen.importapi.adadminstatus.Status
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogStatus
import java.time.LocalDateTime
import java.util.*

data class AdAdminStatusDTO(val uuid: String, val status: Status = Status.RECEIVED, val message: String?, val reference: String,
                            val url: String, val providerId: Long, val created: LocalDateTime, val updated: LocalDateTime)

data class TransferLogDTO(var versionId: Long? = null, @Deprecated("Removing soon, not in use") val provider: ProviderInfo? = null, val providerId: Long, val status: TransferLogStatus = TransferLogStatus.RECEIVED,
                          val message: String? = null, val md5: String="", val items: Int=1, var payload: String?=null, val created: LocalDateTime = LocalDateTime.now(),
                          val updated: LocalDateTime = LocalDateTime.now())

data class AdStatePublicDTO(val uuid: String, @Deprecated("Removing soon, not in use") val provider: ProviderInfo? = null, val providerId: Long, val reference: String, val versionId: Long, val ad: AdDTO,
                            val created: LocalDateTime, val updated: LocalDateTime)

data class ProviderInfo(var id: Long?=null, val jwtid: String = UUID.randomUUID().toString(), val identifier: String)
