package no.nav.arbeidsplassen.importapi.app.test

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestApplicationTest : TestRunningApplication() {

    @Test
    fun `Skal ha fått en mocket versjon av OntologiGateway`() {
        assertTrue("OntologiGateway skal være en anonymous class") { isAnonymous(appCtx.outgoingPortsApplicationContext.ontologiGateway) }
    }

    private fun isAnonymous(obj: Any): Boolean {
        return obj.javaClass.isAnonymousClass
    }
}
