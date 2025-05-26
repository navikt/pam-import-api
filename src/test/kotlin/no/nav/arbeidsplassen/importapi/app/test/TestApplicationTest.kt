package no.nav.arbeidsplassen.importapi.app.test

import kotlin.test.assertTrue
import no.nav.arbeidsplassen.importapi.provider.MockProviderRepository

class TestApplicationTest : TestRunningApplication() {

    // @Test
    // Jeg kjører testene med TestContainer og mocker ikk ut denne
    fun `Skal ha fått en mocket versjon av ProviderRepository`() {
        assertTrue("ProviderRepository skal være en MockProviderRepository") { appCtx.providerRepository is MockProviderRepository }
    }

}
