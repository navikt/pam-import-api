package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLog
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class AdStateDTOTest(private val objectMapper: ObjectMapper,
                     private val providerRepository: ProviderRepository,
                     private val transferLogRepository: TransferLogRepository,
                     private val adstateRepository: AdStateRepository,
                     private val adStateService: AdStateService) {

    @Test
    fun testAdStateAndGenerateDTOJson() {

        val provider = providerRepository.newTestProvider()
        val ads = objectMapper.transferToAdList()
        val transferLog = TransferLog(providerId = provider.id!!, md5 = "123456", payload = "jsonstring", items = 1)
        val transferInDb = transferLogRepository.save(transferLog)
        val ad = ads[0]
        val adState = AdState(jsonPayload = objectMapper.writeValueAsString(ad), providerId = provider.id!!,
            reference = ad.reference, versionId = transferInDb.id!!)
        val save = adstateRepository.save(adState)
        Assertions.assertNotNull(save.id)
        println(objectMapper.writeValueAsString(adStateService.getAdStateByUuid(save.uuid)))
    }


}
