package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


@MicronautTest
class ProviderRepositoryTest(private val providerRepository: ProviderRepository){

    @Test
    fun providerCRUDTest() {
        val provider = providerRepository.newTestProvider()
        assertNotNull(provider.id)
        val id = provider.id!!
        val read = providerRepository.findById(id).get()
        assertEquals(provider.email, read.email)
        assertEquals(provider.identifier, read.identifier)
        val update = read.copy(email="updated@test.test")
        val updated = providerRepository.save(update)
        assertEquals("updated@test.test", updated.email)
        println(updated.toString())
        providerRepository.deleteById(id)
        val deleted = providerRepository.findById(provider.id!!)
        Assertions.assertTrue(deleted.isEmpty)
        val provider2 = Provider(email = "test2@test.test", identifier = "tester2", phone = "12345678")
        val provider3 = Provider(email = "test3@test.test", identifier = "tester3", phone = "12345678")
        val providers = listOf(provider2, provider3)
        providerRepository.saveAll(providers)
        assertEquals(2,providerRepository.list(Pageable.from(0)).content.count())
    }

}
