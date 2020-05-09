package no.nav.arbeidsplassen.importapi.provider

import no.nav.arbeidsplassen.importapi.dto.ProviderInfo
import java.util.*

data class ProviderDTO(var id: Long?=null, val jwtid: String = UUID.randomUUID().toString(), val identifier: String, val email: String, val phone: String)

fun ProviderDTO.info(): ProviderInfo {
    return ProviderInfo(id=id, jwtid = jwtid, identifier = identifier)
}

