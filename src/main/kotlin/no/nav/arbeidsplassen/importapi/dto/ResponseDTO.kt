package no.nav.arbeidsplassen.importapi.dto

import no.nav.arbeidsplassen.importapi.transferlog.TransferLogStatus
import java.time.LocalDateTime
import java.util.*

data class AdAdminStatusDTO(val uuid: String, val status: String, val message: String?, val reference: String,
                            val providerId: Long, val created: LocalDateTime, val updated: LocalDateTime)

data class TransferLogDTO(var versionId: Long? = null, val provider: ProviderDTO, val status: String = TransferLogStatus.RECEIVED.name,
                          val message: String? = null, val md5: String, val items: Int, var payload: String?=null, val created: LocalDateTime = LocalDateTime.now(),
                          val updated: LocalDateTime = LocalDateTime.now())

data class AdStateDTO(val uuid: String, val provider: ProviderDTO, val reference: String, val versionId: Long, val ad: AdDTO,
                      val created: LocalDateTime, val updated: LocalDateTime)

data class ProviderDTO(var id: Long?=null, val jwtid: String = UUID.randomUUID().toString(), val identifier: String, val email: String, val phone: String)
