package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

fun ProviderRepository.newTestProvider(): Provider {
    return save(Provider(username = "tester", email = "tester@tester.test"))
}

