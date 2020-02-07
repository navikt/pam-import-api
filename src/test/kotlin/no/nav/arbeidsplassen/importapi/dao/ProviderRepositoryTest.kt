package no.nav.arbeidsplassen.importapi.dao

import io.micronaut.test.annotation.MicronautTest
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
        val update = read.copy(email="updated@test.test")
        val updated = providerRepository.save(update)
        assertEquals("updated@test.test", updated.email)
        println(updated.toString())
        providerRepository.deleteById(id)
        val deleted = providerRepository.findById(provider.id!!)
        Assertions.assertTrue(deleted.isEmpty)
        val provider2 = Provider(email="test2@test.test", username="tester2")
        val provider3 = Provider(email="test3@test.test", username="tester3")
        val providers = listOf(provider2, provider3)
        providerRepository.saveAll(providers)
        assertEquals(2,providerRepository.findAll().count())
    }

}