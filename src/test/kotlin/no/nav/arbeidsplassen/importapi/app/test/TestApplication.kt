package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.app.testEnv
import no.nav.arbeidsplassen.importapi.startApp

fun main() {
    val localAppCtx = TestApplicationContext(testEnv)
    localAppCtx.applicationContext.startApp()
}
