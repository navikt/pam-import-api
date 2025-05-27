package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.app.testEnv
import no.nav.arbeidsplassen.importapi.startApp

fun main() {
    // TODO Trenger ENV-variabler for db og kafka
    val localAppCtx = TestApplicationContext(TestApplicationContext.testApplicationProperties(testEnv))
    localAppCtx.applicationContext.startApp()
}
