package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import java.util.UUID
import javax.crypto.KeyGenerator
import org.junit.jupiter.api.Test

class JwtTest {

    @Test
    fun generateJwk() {
        val hmacKey = KeyGenerator.getInstance("HmacSha256").generateKey()
        println(
            OctetSequenceKey.Builder(hmacKey)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.HS256)
                .build().keyValue
        )
    }

}
