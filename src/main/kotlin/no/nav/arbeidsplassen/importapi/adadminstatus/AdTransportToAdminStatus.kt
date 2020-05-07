package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.feed.AdTransport


val _PROVIDERID = "_providerid"
val _VERSIONID = "_versionid"

fun AdTransport.toAdminStatus(adminStatusRepository: AdminStatusRepository): AdminStatus {
    return adminStatusRepository.findByUuid(uuid)
            .map { it.copy(status = mapStatus(), versionId = properties[_VERSIONID]?.toLong()!!,
                    message = mapMessage()) }
            .orElseGet{ AdminStatus(uuid = uuid, status = mapStatus(), versionId =
            properties[_VERSIONID]?.toLong()!!, providerId = properties[_PROVIDERID]?.toLong()!!,
                    reference = reference, message = mapMessage()) }
}

private fun AdTransport.mapMessage(): String? {
    if ("REJECTED".equals(status)) {
        return administration.remarks.toString()
    }
    return null
}

private fun AdTransport.mapStatus(): Status {
    return when (administration.status) {
        "DONE" -> Status.DONE
        "PENDING" -> Status.PENDING
        "RECEIVED" -> Status.RECEIVED
        else -> Status.UNKNOWN
    }
}