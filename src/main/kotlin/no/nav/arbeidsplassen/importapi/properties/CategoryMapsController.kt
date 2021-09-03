package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val styrkCodeConverter: StyrkCodeConverter) {

    @Get("/pyrk/occupations")
    fun getPyrkCategoryMap(@QueryValue(defaultValue = "code") sort: String):Map<String, Occupation> {
        return if ("alfa"==sort) styrkCodeConverter.pyrkMap.toList().sortedBy { (k,v) -> v.categoryLevel2 }.toMap()
        else return styrkCodeConverter.pyrkMap.toList().sortedBy { (k,v) -> v.styrkCode }.toMap()
    }

    @Get("/styrk/occupations")
    fun getStyrkCategoryMap(@QueryValue(defaultValue = "code") sort: String): Map<String, Occupation> {
        return if ("alfa"==sort) {
            styrkCodeConverter.occupationMap.toList().sortedBy { (k, v) -> v.categoryLevel2 }.toMap()
        }
        else styrkCodeConverter.occupationMap.toList().sortedBy { (k,v) -> v.styrkCode }.toMap()
    }

}
