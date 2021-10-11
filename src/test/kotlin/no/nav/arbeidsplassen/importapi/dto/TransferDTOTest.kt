package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import jakarta.inject.Inject

@MicronautTest
class TransferDTOTest {

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Test
    fun jsonToTransferDTO() {

        val ads = objectMapper.transferToAdList()
        Assertions.assertEquals(2, ads.size)
        Assertions.assertEquals("Sørumsand barnehage", ads[0].employer!!.businessName)
        Assertions.assertEquals("974220954", ads[1].employer!!.orgnr)
    }
}

