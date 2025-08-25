package no.nav.arbeidsplassen.importapi.common

import io.javalin.http.Context
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.arbeidsplassen.importapi.KONSUMENT_ID_MDC_KEY
import no.nav.arbeidsplassen.importapi.config.hentKonsumentId
import org.slf4j.LoggerFactory

object RequestLogger {

    val REQUESTLOGGER = LoggerFactory.getLogger("access")

    fun logRequest(ctx: Context, ms: Float) {
        REQUESTLOGGER.info(
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
}
