package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.app.testEnv
import no.nav.arbeidsplassen.importapi.startApp

abstract class TestRunningApplication {

    companion object {
        const val lokalUrlBase = "http://localhost:8080"

        @JvmStatic
        val appCtx = TestApplicationContext(testEnv)
        val javalin = appCtx.startApp()
    }

}
