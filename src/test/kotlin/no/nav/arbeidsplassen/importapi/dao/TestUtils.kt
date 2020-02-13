package no.nav.arbeidsplassen.importapi.dao

import no.nav.arbeidsplassen.importapi.provider.Provider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository

fun ProviderRepository.newTestProvider(): Provider {
    return save(Provider(username = "tester", email = "tester@tester.test"))
}

