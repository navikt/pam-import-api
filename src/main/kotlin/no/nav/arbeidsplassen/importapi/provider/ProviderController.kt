package no.nav.arbeidsplassen.importapi.provider
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/internal/providers")
class ProviderController(private val providerService: ProviderService,
                         private val tokenService: TokenService) {
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
        LOG.info("Creating provider ${provider.identifier}")
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
        return HttpResponse.ok(tokenService.token(provider))
    }

}