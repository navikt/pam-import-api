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
        val provider = Provider(email = "test@test.test", username = "tester")
        val created = providerRepository.save(provider)
        assertNotNull(created.id)
        val id = created.id!!
        val read = providerRepository.findById(id).get()
        assertEquals(provider.email, read.email)
        val update = read.copy(email="updated@test.test")
        val updated = providerRepository.save(update)
        assertEquals("updated@test.test", updated.email)
        println(updated.toString())
        providerRepository.deleteById(id)
        val deleted = providerRepository.findById(created.id!!)
        Assertions.assertTrue(deleted.isEmpty)
        val provider2 = Provider(email="test2@test.test", username="tester2")
        val providers = listOf(provider, provider2)
        providerRepository.saveAll(providers)
        assertEquals(2,providerRepository.findAll().count())
    }

}