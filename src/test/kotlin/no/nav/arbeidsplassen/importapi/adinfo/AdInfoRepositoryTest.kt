package no.nav.arbeidsplassen.importapi.adinfo

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AdInfoRepositoryTest(private val repository: AdInfoRepository, private val providerRepository: ProviderRepository) {

    @Test
    fun readAndSave() {
        val provider = providerRepository.newTestProvider()
        val first = repository.save(AdInfo(providerId=provider.id!!, uuid = UUID.randomUUID().toString(), reference = UUID.randomUUID().toString(), activity = mutableMapOf(Pair("pageviews", 10))))
        assertNotNull(first.id)
        val inDb = repository.findById(first.id).get()
        assertNotNull(inDb)
        assertEquals(10, inDb.activity["pageviews"])
    }
}
