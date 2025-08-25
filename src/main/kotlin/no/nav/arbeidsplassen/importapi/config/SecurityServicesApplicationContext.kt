package no.nav.arbeidsplassen.importapi.config

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import no.nav.arbeidsplassen.importapi.provider.ProviderCache
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.JavalinAccessManager
import no.nav.arbeidsplassen.importapi.security.TokenService

class SecurityServicesApplicationContext(
    secretSignatureConfigProperties: SecretSignatureConfigProperties,
    databaseApplicationContext: DatabaseApplicationContext
) {

    val tokenService: TokenService = TokenService(secretSignatureConfigProperties)
    val tokenVerifier: JWSVerifier = MACVerifier(secretSignatureConfigProperties.secret)

    val providerCache = ProviderCache()
    val providerService by lazy {
        ProviderService(
            providerRepository = databaseApplicationContext.providerRepository,
            providerCache = providerCache
        )
    }
    val accessManager = JavalinAccessManager(
        providerService = providerService,
        verifier = tokenVerifier,
    )
}
