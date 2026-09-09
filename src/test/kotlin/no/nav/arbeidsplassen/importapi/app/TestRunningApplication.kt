package no.nav.arbeidsplassen.importapi.app

import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import no.nav.arbeidsplassen.importapi.ApplicationContext
import no.nav.arbeidsplassen.importapi.config.TestApplicationContext
import no.nav.arbeidsplassen.importapi.config.TestRepositoriesContext
import no.nav.arbeidsplassen.importapi.kjørFlywayMigreringer
import no.nav.arbeidsplassen.importapi.startApp
import no.nav.arbeidsplassen.importapi.startJavalin
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext

@ExtendWith(TestRunningApplication.Lifecycle::class)
abstract class TestRunningApplication {

    companion object {
        private val localEnv: MutableMap<String, String> = mutableMapOf()

        @JvmStatic
        val appCtx: ApplicationContext = TestApplicationContext(localEnv).applicationContext
        val javaLin = appCtx.startApp(port = 0)
        val lokalUrlBase = "http://localhost:${javaLin.port()}/stillingsimport/"
    }

    class Lifecycle : BeforeAllCallback {
        override fun beforeAll(context: ExtensionContext) {
            context.root.getStore(ExtensionContext.Namespace.create(TestRunningApplication::class.java))
                .getOrComputeIfAbsent("application") {
                    ExtensionContext.Store.CloseableResource {
                        try {
                            javaLin.stop()
                        } finally {
                            appCtx.databaseApplicationContext.dataSource.close()
                        }
                    }
                }
        }
    }
}

abstract class TestRepositories {

    companion object {
        const val lokalUrlBase = "http://localhost:9028/stillingsimport/"
        private val localEnv: MutableMap<String, String> = mutableMapOf()

        @JvmStatic
        val appCtx: TestRepositoriesContext = TestRepositoriesContext(localEnv)
        val javalin: Javalin = appCtx.startApp()


        private fun TestRepositoriesContext.startApp(): Javalin {

            kjørFlywayMigreringer(this.databaseApplicationContext.dataSource)

            val javalin = startJavalin(
                port = 8088,
                jsonMapper = JavalinJackson(this.baseServicesApplicationContext.objectMapper),
                meterRegistry = this.baseServicesApplicationContext.prometheusRegistry,
                accessManager = this.securityServicesApplicationContext.accessManager,
                onServerStartedListeners = emptyList(),
                onServerStoppingListeners = emptyList()
            )
            return javalin
        }
    }
}
