package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.app.TestHttpClient
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.transferToAdList
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferLogControllerTest : TestRunningApplication() {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogControllerTest::class.java)
    }

    private val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository
    private val transferLogRepository: TransferLogRepository =
        appCtx.databaseApplicationContext.transferLogRepository
    private val transferLogTasks: TransferLogTasks = appCtx.servicesApplicationContext.transferLogTasks
    private val adStateRepository: AdStateRepository = appCtx.databaseApplicationContext.adStateRepository
    private val adOutboxRepository: AdOutboxRepository = appCtx.databaseApplicationContext.adOutboxRepository
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate
    private val objectMapper: ObjectMapper by lazy { appCtx.baseServicesApplicationContext.objectMapper }
    private val tokenService: TokenService by lazy { appCtx.securityServicesApplicationContext.tokenService }

    private val client = TestHttpClient(lokalUrlBase, objectMapper)

    @AfterEach
    fun teardown() {
        val providerId = providerRepository.findByIdentifier("test")!!.id!!
        transferLogRepository.deleteByProviderId(providerId)
        adStateRepository.deleteByProviderId(providerId)
        providerRepository.deleteById(providerId)

        fun AdOutboxRepository.slettAlle() {
            txTemplate.doInTransaction { ctx ->
                val connection = ctx.connection()
                connection.prepareStatement("""DELETE from ad_outbox""").executeUpdate()
            }
        }
        adOutboxRepository.slettAlle()
    }

    @Test
    fun `create provider and then upload ads in batches`() {

        // create provider
        val adminToken = tokenService.adminToken()
        val message = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, message.statusCode())
        val provider = objectMapper.readValue(message.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        LOG.info(provider.toString())

        // start the transfer
        val response = client.post(
            "api/v1/transfers/batch/${provider.id}",
            objectMapper.transferToAdList(),
            providertoken
        )
        assertEquals(201, response.statusCode())
        val versionId = objectMapper.readValue(response.body(), TransferLogDTO::class.java).versionId
        LOG.info("VersionId: $versionId")

        LOG.info("GETing for provider")
        val get = client.get("api/v1/transfers/${provider.id}/versions/$versionId", providertoken)
        assertEquals(200, get.statusCode())
        LOG.info("Body: ${objectMapper.readValue(get.body(), TransferLogDTO::class.java)}")

        LOG.info("GETing for admin")
        val get2 = client.get("api/v1/transfers/${provider.id}/versions/$versionId", adminToken)
        assertEquals(200, get2.statusCode())
        objectMapper.readValue(get2.body(), TransferLogDTO::class.java)

        LOG.info("Internal GETing for admin")
        val get3 = client.get("internal/transfers/$versionId", adminToken)
        assertEquals(200, get3.statusCode())
        objectMapper.readValue(get3.body(), TransferLogDTO::class.java)

    }

    @Test
    fun `create provider then upload one ad in stream`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
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
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLog = objectMapper.readValue(response.body(), TransferLogDTO::class.java)
        assertEquals(TransferLogStatus.RECEIVED, transferLog.status)

        // Den neste testen er avhengig av at en scheduled jobb er kjørt som oppretter AdState for TransferLogs
        // Her kjører vi den manuelt:
        transferLogTasks.processTransferLogTask()

        val delete = client.delete("api/v1/transfers/${provider.id}/140095810?delete=true", providertoken)
        assertEquals(200, delete.statusCode())
        objectMapper.readValue(delete.body(), TransferLogDTO::class.java)
    }

    @Test
    fun `create provider then upload two ads in stream`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)

        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
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
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLogs = objectMapper.readerFor(TransferLogDTO::class.java)
            .readValues<TransferLogDTO>(response.body()).use { it.readAll() }
        assertEquals(2, transferLogs.size)
        assertEquals(TransferLogStatus.RECEIVED, transferLogs[0].status)
        assertEquals(TransferLogStatus.RECEIVED, transferLogs[1].status)

    }

    @Test
    fun `create provider then upload one and a half ads in stream with failure`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
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
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLogs = objectMapper.readerFor(TransferLogDTO::class.java)
            .readValues<TransferLogDTO>(response.body()).use { it.readAll() }
        assertEquals(2, transferLogs.size)
        assertEquals(TransferLogStatus.RECEIVED, transferLogs[0].status)
        assertEquals(TransferLogStatus.ERROR, transferLogs[1].status)

    }

    @Test
    fun `create provider then upload zero ads in stream should fail`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
        """.trimIndent(),
            providertoken
        )
        assertEquals(400, response.statusCode())
        assertEquals("", response.body())

    }

    @Test
    fun `create provider then upload gibberish before ad in stream should fail`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
                jfkdfjdk
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
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLog = objectMapper.readValue(response.body(), TransferLogDTO::class.java)
        assertEquals(TransferLogStatus.ERROR, transferLog.status)
        LOG.info("TransferLog: $transferLog")
        assertTrue(transferLog.message!!.contains("JSON Parse error"))

    }

    @Test
    fun `create provider then upload valid json that is not a adDTO in stream should fail`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
            {
              "foo": "bar"
            }
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLog = objectMapper.readValue(response.body(), TransferLogDTO::class.java)
        assertEquals(TransferLogStatus.ERROR, transferLog.status)
        assertTrue(transferLog.message!!.contains("Missing parameter: reference"))

    }

    @Test
    fun `create provider then upload valid AdDTO json in array in stream should fail-ish`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
            [
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
            ]
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLog = objectMapper.readValue(response.body(), TransferLogDTO::class.java)
        LOG.info("TransferLog: $transferLog")
        // Server-koden her var veldig snål, man forventet plutselig å dekode json'en som en List<TransferLogDTO>,
        // ikke som AdDTO som man ellers bruker, og man klarer det på sett og vis fordi man ignorerer de fleste feltene.
        // Og man returnerer RECEIVED selv om man ikke har gjort et kvekk med det man mottok, det burde vært en ERROR
        // (Grunnen til dette var at man endte opp i mekanismen som håndterte feil, den brukte en List.)
        // Så her velger vi aktivt å gjøre det annerledes enn i Micronaut
        // assertEquals(TransferLogStatus.RECEIVED, transferLog?.status)
        // assertNull(transferLog!!.message)
        // assertNull(transferLog.payload)
        // assertNull(transferLog.versionId)
        // assertEquals(0, transferLog.providerId)
        assertEquals(TransferLogStatus.ERROR, transferLog.status)
        assertTrue(transferLog.message!!.contains("Missing parameter"))

    }

    @Test
    fun `create provider then upload empty json array in stream should fail`() {

        val adminToken = tokenService.adminToken()
        val postProvider = client.post(
            "internal/providers",
            ProviderDTO(identifier = "test", email = "test@test.no", phone = "12345678"),
            adminToken
        )
        assertEquals(201, postProvider.statusCode())
        val provider = objectMapper.readValue(postProvider.body(), ProviderDTO::class.java)
        val providertoken = tokenService.token(provider)
        // start the transfer
        val response = client.postJsonStream(
            "api/v1/transfers/${provider.id}", """
            [
            ]
        """.trimIndent(),
            providertoken
        )
        assertEquals(200, response.statusCode())
        val transferLog = objectMapper.readValue(response.body(), TransferLogDTO::class.java)
        assertEquals(TransferLogStatus.ERROR, transferLog.status)
        // Her var det en bug i server-koden i Micronaut, den prøvde å dekode inputen som en liste og så sende tilbake første innslag,
        // men når listen er tom feiler det jo selvsagt..
        LOG.info("Transferlog: $transferLog")
        // assertTrue(transferLog!!.message!!.contains("Error: Index 0 out of bounds for length 0"))
        assertTrue(transferLog.message!!.contains("Missing parameter:"))

    }
}
