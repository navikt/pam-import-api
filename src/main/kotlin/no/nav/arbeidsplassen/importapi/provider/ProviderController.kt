package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.dto.DTOValidation
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import java.util.*

@Controller("/internal/providers")
class ProviderController(private val providerService: ProviderService) {


    @Get("/{uuid}")
    fun getProvider(@PathVariable uuid: UUID): Single<HttpResponse<ProviderDTO>> {
        return Single.just(HttpResponse.ok(providerService.findByUuid(uuid)))
    }

    @Get("/")
    fun getProviders(pageable: Pageable): Single<HttpResponse<Slice<ProviderDTO>>> {
        return Single.just(HttpResponse.ok(providerService.list(pageable)))
    }

    @Post("/")
    fun createProvider(@Body provider: Single<ProviderDTO>): Single<HttpResponse<ProviderDTO>> {
        return provider.map {
            HttpResponse.created(providerService.save(it))
        }
    }

    @Put("/{uuid}")
    fun putProvider(@Body provider: Single<ProviderDTO>, @PathVariable uuid:UUID): Single<HttpResponse<ProviderDTO>> {
        return provider.map {
            val updated = providerService.findByUuid(uuid).copy(
                    email=it.email, userName = it.userName)
            HttpResponse.created(providerService.save(updated))
        }
    }


}