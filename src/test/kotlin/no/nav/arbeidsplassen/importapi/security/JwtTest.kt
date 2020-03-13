package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.micronaut.security.token.jwt.signature.secret.SecretSignatureConfiguration
import java.util.*
import javax.crypto.KeyGenerator
import javax.inject.Singleton

@Singleton
class JwtTest(private val secretConfig: SecretSignatureConfiguration) {

    fun generateJwk(): OctetSequenceKey {
        val hmacKey = KeyGenerator.getInstance("HmacSha256").generateKey()
        return OctetSequenceKey.Builder(hmacKey)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.HS256)
                .build()
    }

    fun jwtToken(role: String = Roles.ROLE_PROVIDER): String {
        val signer = MACSigner(secretConfig.secret)
        val claimsSet = JWTClaimsSet.Builder()
                .subject("test@test.no")
                .jwtID(UUID.randomUUID().toString())
                .issuer("https://arbeidsplassen.nav.no")
                .claim("roles",role)
                .build()
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }


}