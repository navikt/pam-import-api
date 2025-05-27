package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.config.BaseServicesApplicationContext
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TransferDTOTest {

    val objectMapper: ObjectMapper = BaseServicesApplicationContext().objectMapper

    @Test
    fun jsonToTransferDTO() {

        val ads = objectMapper.transferToAdList()
        Assertions.assertEquals(2, ads.size)
        Assertions.assertEquals("Sørumsand barnehage", ads[0].employer!!.businessName)
        Assertions.assertEquals("974220954", ads[1].employer!!.orgnr)
    }
}
