package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.ontologi.EscoDTO
import no.nav.arbeidsplassen.importapi.ontologi.KonseptGrupperingDTO
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead

@Factory
class TestConfig {


    @Replaces(LokalOntologiGateway::class)
    @Singleton
    class MockLokalOntologiGateway : LokalOntologiGateway("URL") {
        @Override
        override fun hentTypeaheadStilling(stillingstittel : String) : List<Typeahead> {
            return listOf()
        }

        @Override
        override fun hentStyrkOgEscoKonsepterBasertPaJanzz(konseptId: Long): KonseptGrupperingDTO? {
            return KonseptGrupperingDTO(konseptId,
                "janzzForKonseptId=$konseptId", listOf("2221", "2223"), EscoDTO("escolabelForKonseptId=$konseptId",
                    "escouriForKonseptId=$konseptId"
                ))
        }
    }

}