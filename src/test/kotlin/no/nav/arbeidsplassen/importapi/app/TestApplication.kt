package no.nav.arbeidsplassen.importapi.app

import no.nav.arbeidsplassen.importapi.config.TestApplicationContext
import no.nav.arbeidsplassen.importapi.startApp

fun main() {
    val localAppCtx = TestApplicationContext(testEnv)
    localAppCtx.applicationContext.startApp()
}
