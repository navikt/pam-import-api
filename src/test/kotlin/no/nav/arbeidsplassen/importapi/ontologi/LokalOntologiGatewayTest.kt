package no.nav.arbeidsplassen.importapi.ontologi

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.PropertySource
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PropertySource(value = [Property(name="pam.ontologi.typeahead.url", value = "http://unittest-pam-ontologi")])
class LokalOntologiGatewayTest{

    lateinit var lokalOntologiGateway: LokalOntologiGateway

    @Test
    fun `vi tester`() {
        val server = MockWebServer()
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(ontologiBody(konseptId = "19564", label = "Møbelsnekker/ interiørsnekker"))

        server.enqueue(response)

        server.start()

        lokalOntologiGateway = LokalOntologiGateway("http://" + server.hostName + ":" + server.port)

        val ontologiResponse = lokalOntologiGateway.hentTypeaheadStillingerFraOntologi()

        assertEquals(19564L, ontologiResponse[0].code)
        assertEquals("Møbelsnekker/ interiørsnekker", ontologiResponse[0].name)

        server.shutdown()
    }

    fun ontologiBody(konseptId: String,label: String) : String = """[{
        "konseptId": "$konseptId",
        "label": "$label"
    }]"""
}