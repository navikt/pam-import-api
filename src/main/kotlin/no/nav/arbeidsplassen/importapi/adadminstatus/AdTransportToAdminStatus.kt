package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.feed.AdTransport
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors


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

fun AdTransport.mapMessage(): String? {
    if ("REJECTED".equals(status)) {
        return translateRemarks(administration.remarks)
    }
    return null
}

fun AdTransport.mapStatus(): Status {
    return when (administration.status) {
        "DONE" -> Status.DONE
        "PENDING" -> Status.PENDING
        "RECEIVED" -> Status.RECEIVED
        else -> Status.UNKNOWN
    }
}

fun translateRemarks(remarks: List<String>): String? {
    return remarks.stream().map{ translateRemark(it) }.collect(Collectors.joining(", "))
}


fun translateRemark(it: String):String {
    return when (it) {
        "NOT_APPROVED_BY_LABOUR_INSPECTION" -> "Not approved by labour inspection/Arbeidsgiver er ikke godkjent av Arbeidstilsynet"
        "NO_EMPLOYMENT" -> "NO employment/Ingen ansettelsesforhold"
        "DUPLICATE" -> "Duplicate/duplikat"
        "DISCRIMINATING" -> "Discriminating/Diskriminerende annonse"
        "REJECT_BECAUSE_CAPACITY" -> "Reject because of capacity/Ikke nok ressurs"
        "FOREIGN_JOB" -> "Foreign job/En utenlandsk jobb"
        "COLLECTION_JOB" -> "Collection of jobs/En annonse som inneholder for mange forskjellige type annonser"
        else -> "Unknown reason/ukjent grunn"
    }
}


