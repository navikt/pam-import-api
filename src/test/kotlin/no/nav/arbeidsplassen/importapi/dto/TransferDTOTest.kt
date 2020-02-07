package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.dto.Transfer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class TransferDTOTest {

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Test
    fun jsonToTransferDTO() {

        val transfer = objectMapper.readValue(TransferDTOTest::class.java.getResourceAsStream("/transfer-ads.json"), Transfer::class.java)
        Assertions.assertEquals(2, transfer.ads.size)
        Assertions.assertEquals("Sørumsand barnehage", transfer.ads[0].employer!!.businessName)

    }
}