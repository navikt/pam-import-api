package no.nav.arbeidsplassen.importapi.config

import com.nimbusds.jwt.JWTClaimsSet
import io.javalin.http.Context
import no.nav.arbeidsplassen.importapi.KONSUMENT_ID_MDC_KEY

fun Map<String, String>.lesEnvVarEllerKastFeil(envVarNavn: String): String =
    this[envVarNavn] ?: throw IllegalArgumentException("$envVarNavn er ikke satt")


fun Context.hentKonsumentId(): String? =
    attribute(KONSUMENT_ID_MDC_KEY)

fun Context.setClaims(claims: JWTClaimsSet) {
    attribute("claims", claims)
}

fun Context.getClaims(): JWTClaimsSet? =
    attribute("claims")

fun Context.setAccessToken(token: String) {
    attribute("access_token", token)
}

fun Context.getAccessToken(): String? =
    attribute("access_token")

// Sikrer at body kun inneholder tegn som er lovlig JSON
fun Context.sanitizedBody(): String =
    this.body().replace(Regex("[^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]"), " ")
        .replace("\\u0000", "")
