package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adpuls.AdPuls
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsRepository
import no.nav.arbeidsplassen.importapi.adpuls.PulsEventType
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class AdPulsRepositoryTest(private val repository: AdPulsRepository, private val providerRepository: ProviderRepository) {

    @Test
    fun readAndSave() {
        val provider = providerRepository.newTestProvider()
        val first = repository.save(AdPuls(providerId=provider.id!!, uuid = UUID.randomUUID().toString(), reference = UUID.randomUUID().toString(), type = PulsEventType.pageviews, total = 10))
        assertNotNull(first.id)
        val inDb = repository.findById(first.id!!).get()
        assertNotNull(inDb)
        assertEquals(10, inDb.total)
        val new = inDb.copy(total = 20)
        repository.save(new)
        val typeUuid = repository.findByUuidAndType(first.uuid, first.type)
        assertNotNull(typeUuid)
        assertEquals(first.id, typeUuid!!.id)
        assertEquals(20, typeUuid!!.total)
    }
}
