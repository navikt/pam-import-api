package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.feed.AdTransport


val _PROVIDERID = "_providerid"
val _VERSIONID = "_versionid"

fun AdTransport.toAdminStatus(adminStatusRepository: AdminStatusRepository): AdminStatus {
    return adminStatusRepository.findByUuid(uuid)
            .map { it.copy(status = mapStatus(), versionId = properties[_VERSIONID]?.toLong()!!,
                    message = mapMessage(), publishStatus = mapPublishingStatus())}
            .orElseGet{ AdminStatus(uuid = uuid, status = mapStatus(), versionId =
            properties[_VERSIONID]?.toLong()!!, providerId = properties[_PROVIDERID]?.toLong()!!,
                    reference = reference, message = mapMessage(), publishStatus = mapPublishingStatus()) }
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

private fun AdTransport.mapPublishingStatus(): PublishStatus {
    return when (status) {
        "INACTIVE" -> PublishStatus.INACTIVE
        "ACTIVE" -> PublishStatus.ACTIVE
        "REJECTED" -> PublishStatus.REJECTED
        "STOPPED" -> PublishStatus.STOPPED
        "DELETED" -> PublishStatus.DELETED
        else -> PublishStatus.UNKNOWN
    }
}
