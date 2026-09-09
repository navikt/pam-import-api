package no.nav.arbeidsplassen.importapi.adpuls

import java.time.LocalDateTime
import java.util.UUID
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import net.javacrumbs.jsonunit.JsonAssert.whenIgnoringPaths
import no.nav.arbeidsplassen.importapi.app.TestRunningApplication
import no.nav.arbeidsplassen.importapi.app.TestHttpClient
import no.nav.arbeidsplassen.importapi.dao.findTestProvider
import no.nav.arbeidsplassen.importapi.dao.newTestProvider
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdPulsControllerTest() : TestRunningApplication() {

    private val tokenService: TokenService = appCtx.securityServicesApplicationContext.tokenService

    private val client = TestHttpClient(lokalUrlBase, appCtx.baseServicesApplicationContext.objectMapper)

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
        val response = client.get(
            "api/v1/stats/${providerId}?from=${from}&sort=created,asc&size=10&page=1",
            adminToken
        )
        assertEquals(200, response.statusCode())

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
        val response = client.get("api/v1/stats/${providerId}?from=${from}", adminToken)
        assertEquals(200, response.statusCode())

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
        )
        LOG.info("Body" + response.body())
    }

    @Test
    fun `GET med feilaktig sort skal gi 400`() {
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val response = client.get(
            "api/v1/stats/${providerId}?from=${from}&sort=foobar,asc&size=10&page=1",
            adminToken
        )
        assertEquals(400, response.statusCode())
    }

    @Test
    fun `GET med sort men uten direction skal gi default direction`() {
        val providerId = providerRepository.findTestProvider().id!!
        val from = LocalDateTime.now().minusHours(20)
        val adminToken = tokenService.adminToken()
        val response = client.get(
            "api/v1/stats/${providerId}?from=${from}&sort=created&size=10&page=1",
            adminToken
        )
        assertEquals(200, response.statusCode())

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
        )
        LOG.info("Body" + response.body())
    }
}
