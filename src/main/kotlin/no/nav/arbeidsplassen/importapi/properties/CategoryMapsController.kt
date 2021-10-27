package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import java.security.KeyStore
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val styrkCodeConverter: StyrkCodeConverter) {

    @Get("/pyrk/occupations")
    fun getPyrkCategoryMap():Map<String, PyrkOccupation> {
        return styrkCodeConverter.occupationMap.toList().distinctBy { (k,v) -> v.categoryLevel2 }.sortedBy {(k,v) -> v.styrkCode }.toMap().mapValues{it.value.simplyfy()}
    }

    @Get("/styrk/occupations")
    fun getStyrkCategoryMap(@QueryValue(defaultValue = "code") sort: String): Map<String, Occupation> {
        return if ("alfa"==sort) {
            styrkCodeConverter.occupationMap.toList().sortedBy { (k, v) -> v.styrkDescription }.toMap()
        }
        else styrkCodeConverter.occupationMap.toList().sortedBy { (k,v) -> v.styrkCode }.toMap()
    }

}

data class PyrkOccupation(val styrkCode: String, val categoryLevel1: String, val categoryLevel2: String)
fun Occupation.simplyfy(): PyrkOccupation = PyrkOccupation(styrkCode, categoryLevel1, categoryLevel2)
