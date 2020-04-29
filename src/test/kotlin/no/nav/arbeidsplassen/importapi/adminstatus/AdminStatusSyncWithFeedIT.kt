package no.nav.arbeidsplassen.importapi.adminstatus

import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusSyncWithFeed
import org.junit.jupiter.api.Test

@MicronautTest
class AdminStatusSyncWithFeedIT(private val adminStatusSyncWithFeed: AdminStatusSyncWithFeed) {


    @Test
    fun `get ads from feed and sync with adminstatus`() {
        adminStatusSyncWithFeed.syncAdminStatus()
    }

}