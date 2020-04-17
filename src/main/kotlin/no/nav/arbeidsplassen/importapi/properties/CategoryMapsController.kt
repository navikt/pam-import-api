package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Single
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val styrkCodeConverter: StyrkCodeConverter) {

    @Get("/pyrk/occupations")
    fun getPyrkCategoryMap():MutableMap<String, Occupation> {
        return styrkCodeConverter.pyrkMap
    }

    @Get("/styrk/occupations")
    fun getStyrkCategoryMap(): MutableMap<String, Occupation> {
        return styrkCodeConverter.occupationMap
    }

}