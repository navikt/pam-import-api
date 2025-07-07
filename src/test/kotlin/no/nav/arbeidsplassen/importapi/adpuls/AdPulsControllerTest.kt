package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import net.javacrumbs.jsonunit.JsonAssert.whenIgnoringPaths
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.dao.findTestProvider
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory


// TODO: Denne kjører ikke grønt, og jeg forstår ikke hvorfor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @Disabled
class AdPulsControllerTest() : TestRunningApplication() {

    private val tokenService: TokenService = appCtx.securityServicesApplicationContext.tokenService
    private val txTemplate: TxTemplate = appCtx.databaseApplicationContext.txTemplate

    private val client: Rx3HttpClient = Rx3HttpClient.create(URI(lokalUrlBase).toURL())

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsControllerTest::class.java)

        val repository: AdPulsRepository = appCtx.databaseApplicationContext.adPulsRepository
        val providerRepository: ProviderRepository = appCtx.databaseApplicationContext.providerRepository


        @BeforeAll
        @JvmStatic
        fun setup() {
            val provider = providerRepository.newTestProvider()
            val first = repository.save(
                AdPuls(
                    providerId = provider.id!!,
                    uuid = UUID.randomUUID().toString(),
                    reference = UUID.randomUUID().toString(),
                    type = PulsEventType.pageviews,
                    total = 10
                )
            )
            val inDb = repository.findById(first.id!!)!!
            val new = inDb.copy(total = 20)
            repository.save(new)

            repository.saveAll(
                (1..20).map {
                    // Thread.sleep(1)
                    AdPuls(
                        providerId = provider.id!!,
                        uuid = UUID.randomUUID().toString(),
                        reference = UUID.randomUUID().toString(),
                        type = PulsEventType.pageviews,
                        total = it.toLong()
                    )
                }
            )
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            val providerId = providerRepository.findTestProvider().id!!
            repository.deleteByProviderId(providerId)
            providerRepository.deleteById(providerId)
        }
    }


    @Test
    fun `GET med sort, size og page skal fungere`() {
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val getRequest =
            HttpRequest.GET<String>("api/v1/stats/${providerId}?from=${from}&sort=created,asc&size=10&page=1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)

        val response: HttpResponse<String> = client.exchange(getRequest, String::class.java).blockingFirst()
        assertEquals(HttpStatus.OK, response.status)

        val expectedJson = """
            {
              "content" : [ ],
              "pageable" : {
                "number" : 1,
                "sort" : {
                  "orderBy" : [ {
                    "property" : "created",
                    "direction" : "ASC",
                    "ignoreCase" : false,
                    "ascending" : true
                  } ]
                },
                "size" : 10
              },
              "numberOfElements" : 10,
              "pageNumber" : 1,
              "empty" : false,
              "offset" : 10,
              "size" : 10
            }
            """.trimIndent()
        assertJsonEquals(
            expectedJson,
            response.body(),
            whenIgnoringPaths("content")
        )
        LOG.info("Body" + response.body())
    }

    @Test
    fun `GET uten sort, size og page skal gi defaults`() {
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val getRequest =
            HttpRequest.GET<String>("api/v1/stats/${providerId}?from=${from}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)

        val response: HttpResponse<String> = client.exchange(getRequest, String::class.java).blockingFirst()
        assertEquals(HttpStatus.OK, response.status)

        val expectedJson = """
            {
              "content" : [ ],
              "pageable" : {
                "number" : 0,
                "sort" : {
                  "orderBy" : [ {
                    "property" : "updated",
                    "direction" : "ASC",
                    "ignoreCase" : false,
                    "ascending" : true
                  } ]
                },
                "size" : 1000
              },
              "numberOfElements" : 21,
              "pageNumber" : 0,
              "empty" : false,
              "offset" : 0,
              "size" : 1000
            }
        """.trimIndent()
        assertJsonEquals(
            expectedJson,
            response.body(),
            whenIgnoringPaths(
                "content",
                "pageable.sort.orderBy"
            )
            // Micronaut gir en tom orderBy når man ikke sender noe inn
            // Her er spørringen som brukes:
            // SELECT ad_puls_."id",ad_puls_."provider_id",ad_puls_."uuid",ad_puls_."reference",ad_puls_."type",ad_puls_."total",ad_puls_."created",ad_puls_."updated" FROM "ad_puls" ad_puls_ WHERE (ad_puls_."provider_id" = ? AND ad_puls_."updated" > ?) LIMIT 1000
            // Dette er en dum spørring, så dette vil vi ikke videreføre når vi skriver vekk Micronaut Data. Derfor legger jeg til pageable.sort.orderBy på whenIgnoringPaths
        )
        LOG.info("Body" + response.body())
    }

    @Test
    fun `GET med feilaktig sort skal gi 400`() {
        // HPH : Denne er endret fra Micronaut til Javalin, i Micronaut returnerte den 500,
        // men jeg synes 400 gir mer mening og tenker det er en grei endring..
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val getRequest =
            HttpRequest.GET<String>("api/v1/stats/${providerId}?from=${from}&sort=foobar,asc&size=10&page=1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)

        try {
            client.exchange(getRequest, String::class.java).blockingFirst()
            fail("Should have thrown HttpClientResponseException")
            // Litt usikker på hvorfor den automatisk mapper til en exception i stedet for at dette fungerer:
            // assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status)
        } catch (ex: HttpClientResponseException) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.status)
        }
    }

    @Test
    fun `GET med sort men uten direction skal gi default direction`() {
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val getRequest =
            HttpRequest.GET<String>("api/v1/stats/${providerId}?from=${from}&sort=created&size=10&page=1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(adminToken)

        val response: HttpResponse<String> = client.exchange(getRequest, String::class.java).blockingFirst()
        assertEquals(HttpStatus.OK, response.status)

        val expectedJson = """
            {
              "content" : [ ],
              "pageable" : {
                "number" : 1,
                "sort" : {
                  "orderBy" : [ {
                    "property" : "updated",
                    "direction" : "ASC",
                    "ignoreCase" : false,
                    "ascending" : true
                  } ]
                },
                "size" : 10
              },
              "numberOfElements" : 10,
              "pageNumber" : 1,
              "empty" : false,
              "offset" : 10,
              "size" : 10
            }
        """.trimIndent()
        assertJsonEquals(
            expectedJson,
            response.body(),
            whenIgnoringPaths(
                "content",
                "pageable.sort.orderBy"
            )
            // Micronaut gir en tom orderBy når man ikke sender noe inn
            // Her er spørringen som brukes:
            // SELECT ad_puls_."id",ad_puls_."provider_id",ad_puls_."uuid",ad_puls_."reference",ad_puls_."type",ad_puls_."total",ad_puls_."created",ad_puls_."updated" FROM "ad_puls" ad_puls_ WHERE (ad_puls_."provider_id" = ? AND ad_puls_."updated" > ?) LIMIT 1000
            // Dette er en dum spørring, så dette vil vi ikke videreføre når vi skriver vekk Micronaut Data. Derfor legger jeg til pageable.sort.orderBy på whenIgnoringPaths, vi vil legge til default sortering
        )
        LOG.info("Body" + response.body())
    }
}
