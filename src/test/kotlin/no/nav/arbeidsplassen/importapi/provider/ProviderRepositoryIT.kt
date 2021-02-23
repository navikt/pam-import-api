package no.nav.arbeidsplassen.importapi.provider


import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class ProviderRepositoryIT(val providerRepository: ProviderRepository) {


    @Test
    fun providerMigrationTest() {
        val provider = Provider(id=10001,jwtid = UUID.randomUUID().toString(), identifier = "tester", email="tester@test.no",
                phone="12345678")
        val provider2 = Provider(id=10002,jwtid = UUID.randomUUID().toString(), identifier = "tester2", email="tester2@test.no",
                phone="12345678")
        val providers = listOf(provider, provider2)
        providerRepository.saveOnMigrate(providers)

        providerRepository.list(Pageable.unpaged()).forEach {
            println("${it.id} ${it.identifier}")
        }
    }

}
