package no.nav.arbeidsplassen.importapi.adminstatus

import io.micronaut.test.annotation.MicronautTest
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusSync
import org.junit.jupiter.api.Test

@MicronautTest
class AdminStatusSyncIT(private val adminStatusSync: AdminStatusSync) {


    @Test
    fun `get ads from feed and sync with adminstatus`() {
        adminStatusSync.syncAdminStatus()
    }

}