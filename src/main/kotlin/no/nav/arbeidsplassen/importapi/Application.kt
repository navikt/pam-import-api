package no.nav.arbeidsplassen.importapi


// import io.swagger.v3.oas.annotations.OpenAPIDefinition
// import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
// import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
// import io.swagger.v3.oas.annotations.info.Info
// import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.json.JavalinJackson
import io.javalin.micrometer.MicrometerPlugin
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.UUID
import javax.sql.DataSource
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.arbeidsplassen.importapi.config.hentKonsumentId
import no.nav.arbeidsplassen.importapi.security.ForbiddenException
import no.nav.arbeidsplassen.importapi.security.JavalinAccessManager
import no.nav.arbeidsplassen.importapi.security.NotFoundException
import no.nav.arbeidsplassen.importapi.security.UnauthorizedException
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        accessManager = accessManager
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
    accessManager: JavalinAccessManager
): Javalin {
    val requestLogger = LoggerFactory.getLogger("access")
    val log = LoggerFactory.getLogger("no.nav.arbeid.pam_javalin_template")
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
            )
                logRequest(ctx, ms, requestLogger)
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
    }.exception(NotFoundException::class.java) { e, ctx ->
        log.info("NotFoundException: ${e.message}", e)
        ctx.status(404).result(e.message ?: "")
    }.exception(ForbiddenException::class.java) { e, ctx ->
        log.info("ForbiddenException: ${e.message}", e)
        ctx.status(403).result(e.message ?: "")
    }.exception(UnauthorizedException::class.java) { e, ctx ->
        log.info("UnauthorizedException: ${e.message}", e)
        ctx.status(401).result(e.message ?: "")
    }.exception(IllegalArgumentException::class.java) { e, ctx ->
        log.info("IllegalArgumentException: ${e.message}", e)
        ctx.status(400).result(e.message ?: "")
    }.exception(Exception::class.java) { e, ctx ->
        log.info("Exception: ${e.message}", e)
        ctx.status(500).result(e.message ?: "")
    }.start(port)
}

fun logRequest(ctx: Context, ms: Float, log: Logger) {
    log.info(
        "${ctx.method()} ${ctx.url()} ${ctx.statusCode()}",
        kv("konsument_id", ctx.attribute<String>(KONSUMENT_ID_MDC_KEY)),
        kv("method", ctx.method()),
        kv("requested_uri", ctx.path()),
        kv("requested_url", ctx.url()),
        kv("protocol", ctx.protocol()),
        kv("status_code", ctx.statusCode()),
        kv("TraceId", "${ctx.attribute<String>("TraceId")}"),
        kv(KONSUMENT_ID_MDC_KEY, "${ctx.hentKonsumentId()}"),
        kv("elapsed_ms", "$ms")
    )
}
