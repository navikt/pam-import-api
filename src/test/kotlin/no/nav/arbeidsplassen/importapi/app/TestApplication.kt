package no.nav.arbeidsplassen.importapi.app

import no.nav.arbeidsplassen.importapi.config.LocalApplicationContext
import no.nav.arbeidsplassen.importapi.startApp

fun main() {
    val localAppCtx = LocalApplicationContext(testEnv)
    localAppCtx.applicationContext.startApp()
}
