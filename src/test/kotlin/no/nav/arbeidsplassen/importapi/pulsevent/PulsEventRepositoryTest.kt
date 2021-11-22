package no.nav.arbeidsplassen.importapi.pulsevent

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class PulsEventRepositoryTest(private val repository: PulsEventRepository, private val providerRepository: ProviderRepository) {

    @Test
    fun readAndSave() {
        val provider = providerRepository.newTestProvider()
        val first = repository.save(PulsEvent(providerId=provider.id!!, uuid = UUID.randomUUID().toString(),
            type = "Stilling visning", total = 5))
        assertNotNull(first.id)
        val inDb = repository.findById(first.id).get()
        assertNotNull(inDb)
        assertEquals(5,inDb.total)
        val updated = repository.save(inDb.copy(total=10))
        assertEquals(first.id, updated.id)
        assertEquals(10, updated.total)
    }
}
