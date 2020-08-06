package no.nav.arbeidsplassen.importapi.dto

import java.time.LocalDateTime

data class AdStateDTO(val uuid: String, val provider: ProviderInfo, val reference: String, val versionId: Long, val ad: AdDTO,
                      val created: LocalDateTime, val updated: LocalDateTime)
