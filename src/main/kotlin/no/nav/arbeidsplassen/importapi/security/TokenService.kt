package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.micronaut.security.token.jwt.signature.secret.SecretSignatureConfiguration
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import java.util.*
import javax.inject.Singleton

@Singleton
class TokenService(private val secretSignatureConfiguration: SecretSignatureConfiguration) {


    fun token(provider: ProviderDTO): String {
        val signer = MACSigner(secretSignatureConfiguration.secret)
        val claimsSet = JWTClaimsSet.Builder()
                .subject(provider.email)
                .jwtID(provider.jwtid)
                .issuer("https://arbeidsplassen.nav.no")
                .issueTime(Date())
                .claim("roles", Roles.ROLE_PROVIDER)
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
                .claim("roles",Roles.ROLE_ADMIN)
                .build()
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}