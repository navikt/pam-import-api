package no.nav.arbeidsplassen.importapi.provider
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.token.jwt.signature.secret.SecretSignatureConfiguration
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.security.RolesAllowed

@RolesAllowed(value = [Roles.ROLE_ADMIN])
@Controller("/internal/providers")
class ProviderController(private val providerService: ProviderService,
                         private val secretSignatureConfiguration: SecretSignatureConfiguration) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProviderController::class.java)
    }

    @Get("/{id}")
    fun getProvider(@PathVariable id: Long): HttpResponse<ProviderDTO> {
        return HttpResponse.ok(providerService.findById(id))
    }

    @Get("/")
    fun getProviders(pageable: Pageable): HttpResponse<Slice<ProviderDTO>> {
        return HttpResponse.ok(providerService.list(pageable))
    }

    @Post("/")
    fun createProvider(@Body provider: ProviderDTO): HttpResponse<ProviderDTO> {
        return HttpResponse.created(providerService.save(provider))
    }

    @Put("/{id}")
    fun updateProvider(@Body provider: ProviderDTO, @PathVariable id:Long):HttpResponse<ProviderDTO> {
        val updated = providerService.findById(id).copy(
                    email=provider.email, identifier = provider.identifier, phone = provider.phone)
        return HttpResponse.ok(providerService.save(updated))
    }

    // NOTE set resetKey to true only if you want to disable all previous key.
    @Put("/{id}/token")
    fun generateNewTokeForProvider(@PathVariable id: Long, @QueryValue(defaultValue = "false") resetKey: Boolean): HttpResponse<String> {
        LOG.info("Token generated for provider $id reset key $resetKey")
        val provider = if (resetKey) providerService.save(providerService.findById(id).copy(jwtid = UUID.randomUUID().toString()))
                        else providerService.findById(id)
        return HttpResponse.ok(token(provider))
    }

    private fun token(provider: ProviderDTO): String {
        val signer = MACSigner(secretSignatureConfiguration.secret)
        val claimsSet = JWTClaimsSet.Builder()
                .subject(provider.email)
                .jwtID(provider.jwtid)
                .issuer("https://arbeidsplassen.nav.no")
                .claim("roles", Roles.ROLE_PROVIDER)
                .claim("provider", provider.id)
                .build()
        val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }


}