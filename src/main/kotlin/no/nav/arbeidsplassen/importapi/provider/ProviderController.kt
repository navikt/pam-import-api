package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import java.util.UUID
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.slf4j.LoggerFactory


@ProviderAllowed(value = [Roles.ROLE_ADMIN])
@Controller("/internal/providers")
// @Hidden
class ProviderController(
    private val providerService: ProviderService,
    private val tokenService: TokenService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProviderController::class.java)
    }

    @Get("/{id}")
    fun getProvider(@PathVariable id: Long): HttpResponse<ProviderDTO> {
        return HttpResponse.ok(providerService.findById(id))
    }

    @Post("/")
    fun createProvider(@Body provider: ProviderDTO): HttpResponse<ProviderDTO> {
        LOG.info("Creating provider ${provider.identifier}")
        return HttpResponse.created(providerService.save(provider))
    }

    @Put("/{id}")
    fun updateProvider(@Body provider: ProviderDTO, @PathVariable id: Long): HttpResponse<ProviderDTO> {
        val updated = providerService.findById(id).copy(
            email = provider.email, identifier = provider.identifier, phone = provider.phone
        )
        return HttpResponse.ok(providerService.save(updated))
    }

    // NOTE set resetKey to true only if you want to disable all previous key.
    @Put("/{id}/token")
    fun generateNewTokenForProvider(
        @PathVariable id: Long,
        @QueryValue(defaultValue = "false") resetKey: Boolean
    ): HttpResponse<String> {
        LOG.info("Token generated for provider $id reset key $resetKey")
        val provider =
            if (resetKey) providerService.save(providerService.findById(id).copy(jwtid = UUID.randomUUID().toString()))
            else providerService.findById(id)
        return HttpResponse.ok(tokenService.token(provider))
    }

}
