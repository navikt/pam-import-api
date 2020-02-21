package no.nav.arbeidsplassen.importapi.dto

import java.time.LocalDateTime
import java.util.*

data class AdAdminStatusDTO(val uuid: UUID, val status: String, val message: String?, val reference: String,
                            val providerId: Long, val created: LocalDateTime, val updated: LocalDateTime)

data class TransferLogDTO(val versionId: Long, val status: String, val message: String?, val md5: String,
                          val created: LocalDateTime = LocalDateTime.now(), val updated: LocalDateTime = LocalDateTime.now())

data class AdStateDTO(val uuid: UUID, val providerId: Long, val reference: String, val versionId: Long, val jsonPayload: String,
                      val created: LocalDateTime, val updated: LocalDateTime)