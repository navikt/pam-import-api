package no.nav.arbeidsplassen.importapi.provider

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.reactivex.Single
import no.nav.arbeidsplassen.importapi.dto.ProviderDTO
import java.util.*

@Controller("/internal/providers")
class ProviderController(private val providerService: ProviderService) {


    @Get("/{id}")
    fun getProvider(@PathVariable id: Long): Single<HttpResponse<ProviderDTO>> {
        return Single.just(HttpResponse.ok(providerService.findById(id)))
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

    @Put("/{id}")
    fun putProvider(@Body provider: Single<ProviderDTO>, @PathVariable id:Long): Single<HttpResponse<ProviderDTO>> {
        return provider.map {
            val updated = providerService.findById(id).copy(
                    email=it.email, identifier = it.identifier, phone = it.phone)
            HttpResponse.ok(providerService.save(updated))
        }
    }


}