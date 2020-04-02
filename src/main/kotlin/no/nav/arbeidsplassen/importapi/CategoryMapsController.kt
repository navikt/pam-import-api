package no.nav.arbeidsplassen.importapi

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val styrkCodeConverter: StyrkCodeConverter) {

    @Get("/occupations")
    fun getPyrkCategoryMap(): Single<MutableMap<String, Occupation>> {
        return Single.just(styrkCodeConverter.occupationMap)
    }

    @Get("/styrk/occupations")
    fun getStyrkCategoryMap(): Single<MutableMap<String, Occupation>> {
        return Single.just(styrkCodeConverter.pyrkMap)
    }

}