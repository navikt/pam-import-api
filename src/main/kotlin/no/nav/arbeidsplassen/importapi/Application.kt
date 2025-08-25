package no.nav.arbeidsplassen.importapi


import OpenApiConfig
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.micrometer.MicrometerPlugin
import io.javalin.openapi.plugin.redoc.ReDocPlugin
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.UUID
import javax.sql.DataSource
import no.nav.arbeidsplassen.importapi.common.RequestLogger.logRequest
import no.nav.arbeidsplassen.importapi.config.ApplicationProperties
import no.nav.arbeidsplassen.importapi.config.ApplicationProperties.Companion.NaisApplicationProperties
import no.nav.arbeidsplassen.importapi.config.OnServerShutdown
import no.nav.arbeidsplassen.importapi.config.OnServerStartup
import no.nav.arbeidsplassen.importapi.exception.ImportApiErrorHandler.importApiErrorHandler
import no.nav.arbeidsplassen.importapi.security.JavalinAccessManager
import no.nav.arbeidsplassen.importapi.security.Roles
import org.flywaydb.core.Flyway
import org.slf4j.MDC

fun main() {
    val env = System.getenv()
    val applProperties: ApplicationProperties = NaisApplicationProperties(env)
    val appContext = ApplicationContext(applProperties)
    appContext.startApp()
}

const val KONSUMENT_ID_MDC_KEY = "konsument_id"

fun ApplicationContext.startApp(): Javalin {

    kjørFlywayMigreringer(this.databaseApplicationContext.dataSource)

    val javalin = startJavalin(
        port = 9028,
        jsonMapper = JavalinJackson(this.baseServicesApplicationContext.objectMapper),
        meterRegistry = this.baseServicesApplicationContext.prometheusRegistry,
        accessManager = this.securityServicesApplicationContext.accessManager,
        onServerStartedListeners = listOf(this.servicesApplicationContext, this.schedulerApplicationContext),
        onServerStoppingListeners = listOf(this.servicesApplicationContext, this.schedulerApplicationContext)
    )

    setupAllRoutes(javalin)

    return javalin
}

private fun ApplicationContext.setupAllRoutes(javalin: Javalin) {
    this.controllerApplicationContext.controllers.forEach {
        it.setupRoutes(javalin)
    }
}

fun kjørFlywayMigreringer(dataSource: DataSource) {
    Flyway.configure()
        .loggers("slf4j")
        .dataSource(dataSource)
        .load()
        .migrate()
}

fun startJavalin(
    port: Int,
    jsonMapper: JavalinJackson,
    meterRegistry: PrometheusMeterRegistry,
    accessManager: JavalinAccessManager,
    onServerStartedListeners: List<OnServerStartup>,
    onServerStoppingListeners: List<OnServerShutdown>
): Javalin {

    val micrometerPlugin = MicrometerPlugin { micrometerConfig ->
        micrometerConfig.registry = meterRegistry
    }

    return Javalin.create {
        it.router.contextPath = "/stillingsimport"
        it.router.ignoreTrailingSlashes = true
        it.router.treatMultipleSlashesAsSingleSlash = true
        it.requestLogger.http { ctx, ms ->
            if (!(ctx.path().endsWith("/internal/isReady") ||
                        ctx.path().endsWith("/internal/isAlive") ||
                        ctx.path().endsWith("/internal/prometheus"))
            ) {
                logRequest(ctx, ms)
            }
        }
        it.http.defaultContentType = "application/json"
        it.jsonMapper(jsonMapper)
        it.registerPlugin(micrometerPlugin)
        it.registerPlugin(OpenApiConfig.getOpenApiPlugin())
        it.registerPlugin(SwaggerPlugin { swaggerConfiguration ->
            swaggerConfiguration.roles = arrayOf(Roles.ROLE_UNPROTECTED)
            swaggerConfiguration.documentationPath = "/openapi/arbeidsplassen-1.0-openapi.json"
            swaggerConfiguration.uiPath = "/swagger-ui"
        })
        it.registerPlugin(ReDocPlugin { redocConfiguration ->
            redocConfiguration.roles = arrayOf(Roles.ROLE_UNPROTECTED)
            redocConfiguration.documentationPath = "/openapi/arbeidsplassen-1.0-openapi.json"
            redocConfiguration.uiPath = "/redoc"
        })

    }.beforeMatched { ctx ->
        if (ctx.routeRoles().isEmpty()) {
            return@beforeMatched
        }
        accessManager.manage(ctx, ctx.routeRoles())

    }.before { ctx ->
        val callId = ctx.header("Nav-Call-Id") ?: ctx.header("Nav-CallId") ?: UUID.randomUUID().toString()
        ctx.attribute("TraceId", callId)
        MDC.put("TraceId", callId)
    }.after {
        MDC.remove("TraceId")
        MDC.remove("U")
        MDC.remove(KONSUMENT_ID_MDC_KEY)
    }
        .importApiErrorHandler()
        .events { eventConfig ->
            eventConfig.serverStarted { onServerStartedListeners.forEach { it.onServerStartup() } }
            eventConfig.serverStopping { onServerStoppingListeners.forEach { it.onServerShutdown() } }
        }.start(port)
}
