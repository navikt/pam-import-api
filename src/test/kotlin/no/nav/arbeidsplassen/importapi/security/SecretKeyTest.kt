package no.nav.arbeidsplassen.importapi.security

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@MicronautTest
@Property(name="JWT_SECRET", value = "Thisisaverylongsecretandcanonlybeusedintest")
class SecretKeyTest(private val jwtTest: JwtTest) {

    @Test
    fun generateSecretKey() {
        val jwk = jwtTest.generateJwk()
        assertNotNull(jwk)
        println(jwk)
    }

}