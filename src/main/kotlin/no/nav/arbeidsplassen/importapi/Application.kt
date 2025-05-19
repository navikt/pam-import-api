package no.nav.arbeidsplassen.importapi


// import io.swagger.v3.oas.annotations.OpenAPIDefinition
// import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
// import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
// import io.swagger.v3.oas.annotations.info.Info
// import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.micrometer.MicrometerPlugin
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.UUID
import javax.sql.DataSource
import no.nav.arbeidsplassen.importapi.common.RequestLogger.logRequest
import no.nav.arbeidsplassen.importapi.exception.ImportApiErrorHandler.importApiErrorHandler
import no.nav.arbeidsplassen.importapi.kafka.KafkaListenerStarter
import no.nav.arbeidsplassen.importapi.security.JavalinAccessManager
import org.flywaydb.core.Flyway
import org.slf4j.MDC

/*
@OpenAPIDefinition(
    info = Info(
        title = "Arbeidsplassen import api",
        version = "1.0",
        description = "Import api for available jobs"
    )
)
@SecurityScheme(
    name = "bearer-auth", type = SecuritySchemeType.HTTP, scheme = "bearer",
    `in` = SecuritySchemeIn.HEADER, bearerFormat = "JWT"
)
*/

fun main() {
    val env = System.getenv()
    val appContext = ApplicationContext(env)
    appContext.startApp()
}

const val KONSUMENT_ID_MDC_KEY = "konsument_id"

fun ApplicationContext.startApp(): Javalin {

    kjørFlywayMigreringer(dataSource)

    val accessManager = JavalinAccessManager(
        providerService = providerService,
        verifier = tokenVerifier
    )

    val javalin = startJavalin(
        port = 8080,
        jsonMapper = JavalinJackson(objectMapper),
        meterRegistry = prometheusRegistry,
        accessManager = accessManager,
        kafkaListenerStarter = kafkaListenerStarter,
    )

    setupAllRoutes(javalin)

    return javalin
}

private fun ApplicationContext.setupAllRoutes(javalin: Javalin) {
    naisController.setupRoutes(javalin)
    // providerController.setupRoutes(javalin)
    // transferController.setupRoutes(javalin)
}

fun kjørFlywayMigreringer(dataSource: DataSource) {
    Flyway.configure()
        .loggers("slf4j")
        .dataSource(dataSource)
        .load()
        .migrate()
}

fun startJavalin(
    port: Int = 8080,
    jsonMapper: JavalinJackson,
    meterRegistry: PrometheusMeterRegistry,
    accessManager: JavalinAccessManager,
    kafkaListenerStarter: KafkaListenerStarter
): Javalin {

    val micrometerPlugin = MicrometerPlugin { micrometerConfig ->
        micrometerConfig.registry = meterRegistry
    }

    return Javalin.create {
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
            eventConfig.serverStarted { kafkaListenerStarter.onServerStartup() }
        }.start(port)
}
