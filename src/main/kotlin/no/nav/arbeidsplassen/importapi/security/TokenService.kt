package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfiguration
import no.nav.arbeidsplassen.importapi.provider.ProviderDTO

class TokenService(private val secretSignatureConfiguration: SecretSignatureConfiguration) {

    fun token(provider: ProviderDTO): String {
        val signer = MACSigner(secretSignatureConfiguration.secret)
        val claimsSet = JWTClaimsSet.Builder()
            .subject(provider.email)
            .jwtID(provider.jwtid)
            .issuer("https://arbeidsplassen.nav.no")
            .issueTime(Date())
            .claim("roles", Roles.ROLE_PROVIDER.name)
            .claim("providerId", provider.id)
            .build()
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    fun adminToken(): String {
        val signer = MACSigner(secretSignatureConfiguration.secret)
        val claimsSet = JWTClaimsSet.Builder()
            .subject("admin@arbeidsplassen.nav.no")
            .jwtID(UUID.randomUUID().toString())
            .issuer("https://arbeidsplassen.nav.no")
            .issueTime(Date())
            .claim("roles", Roles.ROLE_ADMIN.name)
            .build()
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}
