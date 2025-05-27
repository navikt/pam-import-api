package no.nav.arbeidsplassen.importapi.ontologi

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LokalOntologiGatewayTest {

    lateinit var lokalOntologiGateway: LokalOntologiGateway

    @Test
    fun `testOntologiResponsParsesTilTypeaheadObjekt`() {
        val server = MockWebServer()
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(ontologiBody(konseptId = "19564", label = "Møbelsnekker/ interiørsnekker"))

        server.enqueue(response)

        server.start()

        lokalOntologiGateway = LokalOntologiGateway("http://unittest-pam-ontologi") // TODO Ble dette riktig?

        val ontologiResponse = lokalOntologiGateway.hentTypeaheadStilling("Møbelsnekker")

        assertEquals(19564L, ontologiResponse[0].code)
        assertEquals("Møbelsnekker/ interiørsnekker", ontologiResponse[0].name)

        server.shutdown()
    }

    @Test
    fun `testKonseptgrupperingResponsParsesTilKonseptGrupperingDTO`() {
        val server = MockWebServer()
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(
                konseptGrupperingResponse(
                    konseptId = "19574",
                    noLabel = "Modellsnekker",
                    styrk08SSB = "12322",
                    styrk08SSB2 = "54535",
                    esco = EscoDTO(label = "modellsnekker", uri = "www.esco.com/modellsnekker")
                )
            )

        server.enqueue(response)

        server.start()

        lokalOntologiGateway = LokalOntologiGateway("http://unittest-pam-ontologi") // TODO Ble dette riktig?

        val konseptGrupperingsresponse = lokalOntologiGateway.hentStyrkOgEscoKonsepterBasertPaJanzz(19574)

        assertEquals(19574L, konseptGrupperingsresponse?.konseptId)
        assertEquals("Modellsnekker", konseptGrupperingsresponse?.noLabel)
        assertEquals(2, konseptGrupperingsresponse?.styrk08SSB?.size)
        assertEquals("54535", konseptGrupperingsresponse?.styrk08SSB?.get(1))
        assertEquals("modellsnekker", konseptGrupperingsresponse?.esco?.label)
        assertEquals("www.esco.com/modellsnekker", konseptGrupperingsresponse?.esco?.uri)

        server.shutdown()
    }

    fun ontologiBody(konseptId: String, label: String): String = """[{
        "konseptId": "$konseptId",
        "label": "$label"
    }]"""

    fun konseptGrupperingResponse(
        konseptId: String,
        noLabel: String?,
        styrk08SSB: String,
        styrk08SSB2: String,
        esco: EscoDTO?
    ): String = """{
        "konseptId": "$konseptId",
        "noLabel": "$noLabel",
        "styrk08SSB": ["$styrk08SSB", "$styrk08SSB2"],
        "esco": {
            "label": "${esco?.label}",
            "uri": "${esco?.uri}"
        }
    }"""
}
