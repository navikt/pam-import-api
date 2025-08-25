package no.nav.arbeidsplassen.importapi.common

import org.slf4j.Logger
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import javax.net.ssl.SSLHandshakeException

fun <T : Any> retryTemplate(
    delayOnFail: Duration = Duration.ofMillis(500),
    requestUrl: String,
    logg: Logger,
    requestShortName: String? = null,
    call: Supplier<T>
): T {
    try {
        val response = meterCall(requestUrl, requestShortName, call)
        if (response is HttpResponse<*>) {
            return if (response.statusCode() in listOf(502, 503, 504)) {
                logg.info("Kall til $requestUrl feilet med ${response.statusCode()} - ${response.body()}, prøver på nytt om ${delayOnFail.toMillis()} ms")
                Thread.sleep((delayOnFail.toMillis()))
                meterCall(requestUrl, requestShortName, call)
            } else {
                response
            }
        }
        return meterCall(requestUrl, requestShortName, call)
    } catch (e: Exception) {
        if (e.isRetryable()) {
            logg.info("Kall til $requestUrl feilet med ${e.javaClass.simpleName} - ${e.message}, prøver på nytt om ${delayOnFail.toMillis()} ms")
            Thread.sleep((delayOnFail.toMillis()))
            return meterCall(requestUrl, requestShortName, call)
        } else {
            throw e
        }
    }
}

fun <T : Any> meterCall(
    requestUrl: String,
    requestShortName: String? = null,
    call: Supplier<T>
): T {
    val now = System.currentTimeMillis()
    var response: T? = null
    try {
        response = call.get()
    } finally {
        var status = "5xx"
        if (response != null && response is HttpResponse<*>) {
            status = "${response.statusCode()}"
        }
        meterOutbound(requestUrl, requestShortName, status, System.currentTimeMillis() - now)
    }
    return response!!
}

fun meterOutbound(requestUrl: String, requestShortName: String?, status: String = "200", forbruktTid: Long) {
    val target =
        requestShortName ?: Regex("https?://([^/]*)/?.*").matchEntire(requestUrl)?.groups?.drop(1)?.firstOrNull()?.value
        ?: requestUrl
    Singeltons.meterRegistry.let { m ->
        val meter = m.timer("outbound_requests", "target", target, "status", status)
        meter.record(forbruktTid, TimeUnit.MILLISECONDS)
    }

}

fun Exception.isRetryable(): Boolean {
    return when (this) {
        is SocketException,
        is SocketTimeoutException,
        is SSLHandshakeException,
        is HttpConnectTimeoutException,
        is HttpTimeoutException
            -> true

        else -> false
    }
}
