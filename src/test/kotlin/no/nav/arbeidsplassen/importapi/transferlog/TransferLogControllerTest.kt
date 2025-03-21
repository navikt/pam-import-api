package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.rxjava3.http.client.Rx3StreamingHttpClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
@Property(name = "JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class TransferLogControllerTest(
    private val objectMapper: ObjectMapper,
    private val tokenService: TokenService
) {

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var client: Rx3HttpClient

    @Inject
    @field:Client("\${micronaut.server.context-path}")
    lateinit var strClient: Rx3StreamingHttpClient

    @Test
    fun `create provider and then upload ads in batches`() {
        // create provider
        val adminToken = tokenService.adminToken()
        val postProvider = HttpRequest.POST(
            "/internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val message: HttpResponse<ProviderDTO> = client.exchange(postProvider, ProviderDTO::class.java).blockingFirst()
        assertEquals(HttpStatus.CREATED, message.status)
        val provider = message.body()
        val providertoken = tokenService.token(provider!!)
        println(provider)

        // start the transfer
        val post = HttpRequest.POST("/api/v1/transfers/batch/${provider.id}", objectMapper.transferToAdList())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(providertoken)
        val response = client.exchange(post, TransferLogDTO::class.java).blockingFirst()
        assertEquals(HttpStatus.CREATED, response.status)
        val get = HttpRequest.GET<String>("/api/v1/transfers/${provider.id}/versions/${response.body()?.versionId}")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(providertoken)
        println(client.exchange(get, TransferLogDTO::class.java).blockingFirst().body())

        val get2 = HttpRequest.GET<String>("/api/v1/transfers/${provider.id}/versions/${response.body()?.versionId}")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        assertEquals(client.exchange(get2, TransferLogDTO::class.java).blockingFirst().status, HttpStatus.OK)
    }

    @Test
    fun `create provider then upload ads in stream`() {
        val adminToken = tokenService.adminToken()
        val postProvider = HttpRequest.POST(
            "/internal/providers",
            ProviderDTO(identifier = "test2", email = "test2@test2.no", phone = "123")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val provider = client.exchange(postProvider, ProviderDTO::class.java).blockingFirst().body()
        val providertoken = tokenService.token(provider!!)
        // start the transfer
        val post = HttpRequest.POST(
            "/api/v1/transfers/${provider.id}", """
            {
              "reference": "140095810",
              "positions": 1,
              "contactList": [
                {
                  "name": "Ola Norman",
                  "title": "Regionleder",
                  "email": "ola.normann@test.com",
                  "phone": "+47 001 00 002"
                },
                {
                  "name": "Kari Normann",
                  "title": "Prosjektleder",
                  "email": "kari.normann@test.com",
                  "phone": "+47 003 00 004"
                }
              ],
              "locationList": [
                {
                    "address": "Veien 2",
                    "postalCode": "0001",
                    "county": "Oslo",
                    "municipal": "Oslo",
                    "country": "Norge"
                }
              ],
              "properties": {
                "extent": "Heltid",
                "employerhomepage": "http://www.sorumsand.norlandiabarnehagene.no",
                "applicationdue": "24.02.2019",
                "keywords": "Barnehage,daglig,leder,styrer",
                "engagementtype": "Fast",
                "employerdescription": "<p>I Norlandia barnehagene vil vi være med å skape livslang lyst til lek og læring. Hos oss er barnets beste alltid i sentrum. Våre medarbeidere er Norlandias viktigste innsatsfaktor, og lederskap vårt viktigste suksesskriterium. Våre ledere er sterke og selvstendige med ansvar for å utvikle lederteam i barnehagene, og for å bidra aktivt inn i Ledergruppen i Regionen. Norlandia Sørumsand vil være tilknyttet Region Øst.</p>\n",
                "starttime": "01.05.2019",
                "applicationemail": "ola.normann@test.com",
                "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=140095810",
                "sector": "Offentlig",
                "applicationlabel": "Søknad Sørumsand"
              },
              "title": "Ønsker du å lede en moderne og veletablert barnehage?",
              "adText": "<p>Nå har du en unik mulighet til å lede en godt faglig og veletablert barnehage. Norlandia Sørumsand barnehage ble etablert i 2006 og har moderne og fleksible oppholdsarealer. Barnehagens satsningsområder er Mat med Smak og Null mobbing i barnehagen.</p>\n<p><strong>Hovedansvarsområder:</strong></p>\n<ul><li>Drifte og utvikle egen barnehage i tråd med gjeldende forskrifter, bestemmelser og Norlandias overordnete strategi</li><li>Personalansvar</li><li>Overordnet faglig ansvar i egen barnehage</li><li>Bidra og medvirke i regionens endrings -og strategiprosesser</li><li>Kvalitet i barnehagen i henhold til konsernets kvalitets- og miljøpolicy</li></ul>\n<p><strong>Ønskede kvalifikasjoner:</strong></p>\n<ul><li>Barnehagelærerutdanning</li><li>Gode lederegenskaper</li><li>Engasjement for mat og miljø</li><li>Økonomiforståelse</li><li>Beslutningsdyktig, proaktiv og løsningsorientert</li><li>Effektiv og evnen til å håndtere flere oppgaver samtidig</li><li>Være motivator og støttespiller for medarbeiderne</li><li>Ha gode strategiske evner</li></ul>\n<p><strong>Vi tilbyr:</strong></p>\n<ul><li>Jobb i et sterkt fagmiljø i stadig utvikling, med et stort handlingsrom innenfor fastsatte rammer</li><li>Korte beslutningsveier og muligheter for personlig og faglig utvikling</li><li>Gode personalfasiliteter</li><li>Konkurransedyktig lønn og gode pensjonsbetingelser</li><li>Gyldig politiattest (ikke eldre enn 3 måneder ved tiltredelse) må fremvises før ansettelse</li></ul>\n<p><em><strong>Dette er en unik mulighet til å få lede en moderne veletablert barnehage.</strong></em></p>\n",
              "privacy": "SHOW_ALL",
              "published": "2019-02-13T12:59:26",
              "expires": "2019-02-24T00:00:00",
              "employer": {
                "id": 255533,
                "reference": "232151232",
                "businessName": "Sørumsand barnehage",
                "orgnr": "989012088",
                "location": {
                  "address": "Sannergata 2",
                  "postalCode": "0566",
                  "country": "Norge",
                  "county": "Oslo",
                  "municipal": "Oslo",
                  "city": "Oslo"
                }
              },
              "categoryList": [
                {
                  "code": "234204",
                  "categoryType": "STYRK08",
                  "name": "Barnehagelærer"
                }
              ]
            }
        """.trimIndent()
        )
            .contentType(MediaType.APPLICATION_JSON_STREAM)
            .accept(MediaType.APPLICATION_JSON_STREAM_TYPE)
            .bearerAuth(providertoken)
        val response = strClient.jsonStream(post, TransferLogDTO::class.java)
        val future = CompletableFuture<TransferLogDTO>()
        response.subscribe { future.complete(it) }
        assertEquals(TransferLogStatus.RECEIVED, future.get().status)
//        Thread.sleep(60000) // takes too long
//        val delete = HttpRequest.DELETE<TransferLogDTO>("/api/v1/transfers/${provider.id}/140095810?delete=true")
//            .contentType(MediaType.APPLICATION_JSON)
//            .accept(MediaType.APPLICATION_JSON_TYPE)
//            .bearerAuth(providertoken)
//        val deleteResp = client.exchange(delete,TransferLogDTO::class.java).blockingFirst().body()
//        println(deleteResp)
    }

    @Test
    fun `create provider then upload ads in stream with failure`() {
        val adminToken = tokenService.adminToken()
        val postProvider = HttpRequest.POST(
            "/internal/providers",
            ProviderDTO(identifier = "test3", email = "test3@test3.no", phone = "124")
        )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .bearerAuth(adminToken)
        val provider = client.exchange(postProvider, ProviderDTO::class.java).blockingFirst().body()
        val providertoken = tokenService.token(provider!!)
        // start the transfer
        val post = HttpRequest.POST(
            "/api/v1/transfers/${provider.id}", """
            {
              "reference": "140095810",
              "positions": 1,
              "contactList": [
                {
                  "name": "Ola Norman",
                  "title": "Regionleder",
                  "email": "ola.normann@test.com",
                  "phone": "+47 001 00 002"
                },
                {
                  "name": "Kari Normann",
                  "title": "Prosjektleder",
                  "email": "kari.normann@test.com",
                  "phone": "+47 003 00 004"
                }
              ],
              "locationList": [
                {
                    "address": "Veien 2",
                    "postalCode": "0001",
                    "county": "Oslo",
                    "municipal": "Oslo",
                    "country": "Norge"
                }
              ],
              "properties": {
                "extent": "Heltid",
                "employerhomepage": "http://www.sorumsand.norlandiabarnehagene.no",
                "applicationdue": "24.02.2019",
                "keywords": "Barnehage,daglig,leder,styrer",
                "engagementtype": "Fast",
                "employerdescription": "<p>I Norlandia barnehagene vil vi være med å skape livslang lyst til lek og læring. Hos oss er barnets beste alltid i sentrum. Våre medarbeidere er Norlandias viktigste innsatsfaktor, og lederskap vårt viktigste suksesskriterium. Våre ledere er sterke og selvstendige med ansvar for å utvikle lederteam i barnehagene, og for å bidra aktivt inn i Ledergruppen i Regionen. Norlandia Sørumsand vil være tilknyttet Region Øst.</p>\n",
                "starttime": "01.05.2019",
                "applicationemail": "ola.normann@test.com",
                "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=140095810",
                "sector": "Offentlig",
                "applicationlabel": "Søknad Sørumsand"
              },
              "title": "Ønsker du å lede en moderne og veletablert barnehage?",
              "adText": "<p>Nå har du en unik mulighet til å lede en godt faglig og veletablert barnehage. Norlandia Sørumsand barnehage ble etablert i 2006 og har moderne og fleksible oppholdsarealer. Barnehagens satsningsområder er Mat med Smak og Null mobbing i barnehagen.</p>\n<p><strong>Hovedansvarsområder:</strong></p>\n<ul><li>Drifte og utvikle egen barnehage i tråd med gjeldende forskrifter, bestemmelser og Norlandias overordnete strategi</li><li>Personalansvar</li><li>Overordnet faglig ansvar i egen barnehage</li><li>Bidra og medvirke i regionens endrings -og strategiprosesser</li><li>Kvalitet i barnehagen i henhold til konsernets kvalitets- og miljøpolicy</li></ul>\n<p><strong>Ønskede kvalifikasjoner:</strong></p>\n<ul><li>Barnehagelærerutdanning</li><li>Gode lederegenskaper</li><li>Engasjement for mat og miljø</li><li>Økonomiforståelse</li><li>Beslutningsdyktig, proaktiv og løsningsorientert</li><li>Effektiv og evnen til å håndtere flere oppgaver samtidig</li><li>Være motivator og støttespiller for medarbeiderne</li><li>Ha gode strategiske evner</li></ul>\n<p><strong>Vi tilbyr:</strong></p>\n<ul><li>Jobb i et sterkt fagmiljø i stadig utvikling, med et stort handlingsrom innenfor fastsatte rammer</li><li>Korte beslutningsveier og muligheter for personlig og faglig utvikling</li><li>Gode personalfasiliteter</li><li>Konkurransedyktig lønn og gode pensjonsbetingelser</li><li>Gyldig politiattest (ikke eldre enn 3 måneder ved tiltredelse) må fremvises før ansettelse</li></ul>\n<p><em><strong>Dette er en unik mulighet til å få lede en moderne veletablert barnehage.</strong></em></p>\n",
              "privacy": "SHOW_ALL",
              "published": "2019-02-13T12:59:26",
              "expires": "2019-02-24T00:00:00",
              "employer": {
                "id": 255533,
                "reference": "232151232",
                "businessName": "Sørumsand barnehage",
                "orgnr": "989012088",
                "location": {
                  "address": "Sannergata 2",
                  "postalCode": "0566",
                  "country": "Norge",
                  "county": "Oslo",
                  "municipal": "Oslo",
                  "city": "Oslo"
                }
              },
              "categoryList": [
                {
                  "code": "234204",
                  "categoryType": "STYRK08",
                  "name": "Barnehagelærer"
                }
              ]
       }            
            {
              "reference": "140095811",
              "positions": 1,
              "contactList": [
                {
                  "name": "Ola Nårman",
                  "title": "Regionleder",
                  "email": "ola.normann@test2.com",
                  "phone": "+47 001 00 002"
                },
                {
                  "name": "Kari Normann",
                  "title": "Prosjektleder",
                  "email": "kari.normann@test.com",
                  "phone": "+47 003 00 004"
                }
              ],
              "locationList": [
                {
                    "address": "Veien 2",
                    "postalCode": "0001",
                    "county": "Oslo",
                    "municipal": "Oslo",
                    "country": "Norge"
                }
              ],
              "properties": {
                "extent": "Heltid",
                "employerhomepage": "http://www.sorumsand.norlandiabarnehagene.no",
                "applicationdue": "24.02.2019",
                "keywords": "Barnehage,daglig,leder,styrer",
                "engagementtype": "Fast",
                "employerdescription": "<p>I Norlandia barnehagene vil vi være med å skape livslang lyst til lek og læring. Hos oss er barnets beste alltid i sentrum. Våre medarbeidere er Norlandias viktigste innsatsfaktor, og lederskap vårt viktigste suksesskriterium. Våre ledere er sterke og selvstendige med ansvar for å utvikle lederteam i barnehagene, og for å bidra aktivt inn i Ledergruppen i Regionen. Norlandia Sørumsand vil være tilknyttet Region Øst.</p>\n",
                "starttime": "01.05.2019",
                "applicationemail": "ola.normann@test.com",
                "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=140095810",
                "sector": "Offentlig",
                "applicationlabel": "Søknad Sørumsand"
              },
              "title": "Ønsker du å lede en litt moderne og etablert barnehage?",
              "adText": "<p>Nå har du en unik mulighet til å lede en godt faglig og veletablert barnehage. Norlandia Sørumsand barnehage ble etablert i 2006 og har moderne og fleksible oppholdsarealer. Barnehagens satsningsområder er Mat med Smak og Null mobbing i barnehagen.</p>\n<p><strong>Hovedansvarsområder:</strong></p>\n<ul><li>Drifte og utvikle egen barnehage i tråd med gjeldende forskrifter, bestemmelser og Norlandias overordnete strategi</li><li>Personalansvar</li><li>Overordnet faglig ansvar i egen barnehage</li><li>Bidra og medvirke i regionens endrings -og strategiprosesser</li><li>Kvalitet i barnehagen i henhold til konsernets kvalitets- og miljøpolicy</li></ul>\n<p><strong>Ønskede kvalifikasjoner:</strong></p>\n<ul><li>Barnehagelærerutdanning</li><li>Gode lederegenskaper</li><li>Engasjement for mat og miljø</li><li>Økonomiforståelse</li><li>Beslutningsdyktig, proaktiv og løsningsorientert</li><li>Effektiv og evnen til å håndtere flere oppgaver samtidig</li><li>Være motivator og støttespiller for medarbeiderne</li><li>Ha gode strategiske evner</li></ul>\n<p><strong>Vi tilbyr:</strong></p>\n<ul><li>Jobb i et sterkt fagmiljø i stadig utvikling, med et stort handlingsrom innenfor fastsatte rammer</li><li>Korte beslutningsveier og muligheter for personlig og faglig utvikling</li><li>Gode personalfasiliteter</li><li>Konkurransedyktig lønn og gode pensjonsbetingelser</li><li>Gyldig politiattest (ikke eldre enn 3 måneder ved tiltredelse) må fremvises før ansettelse</li></ul>\n<p><em><strong>Dette er en unik mulighet til å få lede en moderne veletablert barnehage.</strong></em></p>\n",
              "privacy": "SHOW_ALL",
              "published": "2019-02-13T12:59:26",
              "expires": "2019-02-24T00:00:00",
              "employer": {
                "id": 255533,
                "reference": "232151232",
                "businessName": "Sørumsand barnehage",
                "orgnr": "989012088",
                "location": {
                  "address": "Sannergata 2",
                  "postalCode": "0566",
                  "country": "Norge",
                  "county": "Oslo",
                  "municipal": "Oslo",
                  "city": "Oslo"
                }
              },
              "categoryList": [
                {
                  "code": "234204",
                  "categoryType": "STYRK08",
                  "name": "Barnehagelærer"
                }
              ]
        """.trimIndent()
        )
            .contentType(MediaType.APPLICATION_JSON_STREAM)
            .accept(MediaType.APPLICATION_JSON_STREAM_TYPE)
            .bearerAuth(providertoken)
        val response = strClient.jsonStream(post, TransferLogDTO::class.java)
        val responseQueue = ArrayBlockingQueue<TransferLogDTO>(2)
        response.subscribe { responseQueue.add(it) }
        assertEquals(TransferLogStatus.RECEIVED, responseQueue.poll(5000, TimeUnit.MILLISECONDS)?.status)
        assertEquals(TransferLogStatus.ERROR, responseQueue.poll(2000, TimeUnit.MILLISECONDS)?.status)
    }

}
