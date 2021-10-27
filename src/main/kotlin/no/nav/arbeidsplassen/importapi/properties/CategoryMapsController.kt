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
    fun getPyrkCategoryMap(@QueryValue(defaultValue = "code") sort: String):Map<String, Occupation> {
        return if ("alfa"==sort) styrkCodeConverter.pyrkMap.toList().sortedBy { (k,v) -> v.categoryLevel2 }.toMap()
        else return styrkCodeConverter.pyrkMap.toList().sortedBy { (k,v) -> v.styrkCode }.toMap()
    }

    @Get("/styrk/occupations")
    fun getStyrkCategoryMap(@QueryValue(defaultValue = "code") sort: String): Map<String, PyrkOccupation> {
        return styrkCodeConverter.occupationMap.toList().sortedBy {(k,v) -> v.styrkCode }.toMap().mapValues {it.value.simplyfy()}
    }

}

data class PyrkOccupation(val styrkCode: String, val categoryLevel1: String, val categoryLevel2: String)
fun Occupation.simplyfy(): PyrkOccupation = PyrkOccupation(styrkCode, categoryLevel1, categoryLevel2)
