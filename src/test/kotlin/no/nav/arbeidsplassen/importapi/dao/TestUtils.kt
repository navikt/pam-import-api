package no.nav.arbeidsplassen.importapi.dao

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.provider.Provider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository

class TestUtils {

}

fun ProviderRepository.newTestProvider(): Provider {
    return save(Provider(identifier = "tester", email = "tester@tester.test", phone = "12345678"))
}

fun ObjectMapper.transferJsonString(): String {
    return TestUtils::class.java.getResourceAsStream("/transfer-ads.json").bufferedReader().use { it.readText() }
}

fun ObjectMapper.transferToAdList(): List<AdDTO> {
    return readValue(TestUtils::class.java.getResourceAsStream("/transfer-ads.json"), object: TypeReference<List<AdDTO>>(){})
}
