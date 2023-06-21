package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.arbeidsplassen.importapi.ontologi.OntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.security.KeyStore
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val styrkCodeConverter: StyrkCodeConverter, private val ontologiGateway: OntologiGateway) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CategoryMapsController::class.java)
    }

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

    @Get("/janzz/occupations")
    fun getJanzzCategories(): List<Typeahead> {
        try {
            return ontologiGateway.hentTypeaheadStillingerFraOntologi()
        } catch (e: Exception) {
            log.error("Feilet i henting av typeahead stillinger fra ontologi.", e)
            throw e;
        }
    }

}

data class PyrkOccupation(val styrkCode: String, val categoryLevel1: String, val categoryLevel2: String)
fun Occupation.simplyfy(): PyrkOccupation = PyrkOccupation(styrkCode, categoryLevel1, categoryLevel2)
