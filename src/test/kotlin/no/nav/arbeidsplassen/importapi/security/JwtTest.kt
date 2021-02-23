package no.nav.arbeidsplassen.importapi.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.util.*
import javax.crypto.KeyGenerator

@MicronautTest
class JwtTest {

    @Test
    fun generateJwk()  {
        val hmacKey = KeyGenerator.getInstance("HmacSha256").generateKey()
        println(OctetSequenceKey.Builder(hmacKey)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.HS256)
                .build().keyValue)
    }

}
