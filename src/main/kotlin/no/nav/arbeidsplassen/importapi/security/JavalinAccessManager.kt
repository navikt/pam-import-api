package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.javalin.http.Context
import io.javalin.security.RouteRole
import no.nav.arbeidsplassen.importapi.KONSUMENT_ID_MDC_KEY
import no.nav.arbeidsplassen.importapi.config.setAccessToken
import no.nav.arbeidsplassen.importapi.config.setClaims
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import org.slf4j.LoggerFactory
import org.slf4j.MDC


/**
 */
class JavalinAccessManager(private val providerService: ProviderService, private val verifier: JWSVerifier) {
    // verifier = MACVerifier(secret)
    companion object {
        private val LOG = LoggerFactory.getLogger(JavalinAccessManager::class.java)
    }

    fun manage(ctx: Context, routeRoles: Set<RouteRole>) {
        LOG.info("Sjekker tilgang for ${ctx.path()}")
        if (routeRoles.isEmpty()) {
            LOG.info("Routen er satt opp uten roller, defaulter til å gi tilgang")
            return
        }

        val routeRoller: List<Roles> = routeRoles
            .map {
                if (it is Roles) {
                    it
                } else {
                    LOG.warn("Rolle $it er ikke av klassen Rolle"); null
                }
            }
            .filterNotNull()
        if (routeRoller.isEmpty()) {
            LOG.error("Routen er satt feil, defaulter til å ikke gi tilgang")
            throw UnauthorizedException("Feil i oppsettet, defaulter til ingen tilgang")
        }

        if (routeRoller.contains(Roles.ROLE_UNPROTECTED)) {
            LOG.info("Routen er satt opp med ROLE_UNPROTECTED, gir derfor tilgang")
            return
        }

        val token = hentAccessTokenFraHeader(ctx)
        val signedJWT = SignedJWT.parse(token)
        if (!signedJWT.verify(verifier)) {
            LOG.warn("token $token er IKKE gyldig")
            throw UnauthorizedException("Ugyldig token $token")
        }
        val claimsSet: JWTClaimsSet = signedJWT.jwtClaimsSet
        val authRolle = claimsSet.getStringClaim("roles").asRolle()

        if (claimsSet.issuer != "https://arbeidsplassen.nav.no") {
            LOG.info("Avvist fordi issuer ikke er korrekt ${claimsSet.issuer}")
            throw UnauthorizedException("Ugyldig isuer ${claimsSet.issuer}")
        }

        if (routeRoller.contains(Roles.ROLE_ADMIN) && authRolle == Roles.ROLE_ADMIN) {
            LOG.info("Admin request allow")
            ctx.setClaims(claimsSet)
            ctx.setAccessToken(token)
            return
        }

        val routeParams = ctx.queryParamMap() + ctx.pathParamMap()
        if (routeParams["providerId"] == null) {
            LOG.info("Avvist fordi en provider bare skal kunne aksessere URLS med providerId i pathen")
            throw ForbiddenException("Avvist fordi ulovlig path for provider")
        }
        val providerId = routeParams["providerId"].toString().toLong()
        if (providerId != claimsSet.getLongClaim("providerId")) {
            LOG.info(
                "Avvist fordi providerId $providerId bruker aksesserer ikke matcher providerId fra claims i JWT token ${
                    claimsSet.getLongClaim(
                        "providerId"
                    )
                }"
            )
            throw ForbiddenException("Ugyldig providerId $providerId")
        }

        val provider = providerService.findById(providerId)
        if (provider.jwtid != claimsSet.jwtid) {
            LOG.info("Avvist fordi jwtId registrert på provider ikke matcher med jwtId i JWT token ${claimsSet.jwtid}")
            throw UnauthorizedException("Ugyldig jwtid ${claimsSet.jwtid}")
        }
        if (!routeRoller.contains(authRolle)) {
            LOG.info("Avvist fordi JWT token ikke har den påkrevd rolle, har $authRolle, kreves ${routeRoller.joinToString()}")
            throw ForbiddenException("Ugyldig rolle $authRolle")
        }

        LOG.info("Gir tilgang")
        ctx.setClaims(claimsSet)
        ctx.setAccessToken(token)
        ctx.attribute(KONSUMENT_ID_MDC_KEY, providerId.toString())
        MDC.put(KONSUMENT_ID_MDC_KEY, providerId.toString())
    }

    private fun hentAccessTokenFraHeader(context: Context): String {
        val accessTokenMedBearerPrefix = context.header("Authorization")
            ?: throw UnauthorizedException("Prøvde å hente ut access token men Authorization header finnes ikke")

        return accessTokenMedBearerPrefix.replace("Bearer ", "", ignoreCase = true)
    }

    private fun String.asRolle(): Roles {
        try {
            return Roles.valueOf(this)
        } catch (e: Exception) {
            LOG.error("Ukjent rolle i JWT token: $this", e)
            throw IllegalStateException("Ukjent rolle i JWT token: $this")
        }
    }
}
