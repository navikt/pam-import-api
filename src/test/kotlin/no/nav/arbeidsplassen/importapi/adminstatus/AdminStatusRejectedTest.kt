package no.nav.arbeidsplassen.importapi.adminstatus

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.toAdminStatus
import no.nav.arbeidsplassen.importapi.feed.AdTransport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class AdminStatusRejectedTest(private val objectMapper: ObjectMapper, private val adminStatusRepository: AdminStatusRepository) {

    @Test
    fun `ads from pam-ad must have correct adminstatus`() {
        val pamAd = objectMapper.readValue(AdminStatusRejectedTest::class.java.getResourceAsStream("/contracts/pam-ad.json"),
               AdTransport::class.java)
        assertEquals(pamAd.status, "REJECTED")
        val admin = pamAd.toAdminStatus(adminStatusRepository)
        println(admin.message)
    }
}
