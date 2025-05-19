package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CategoryMapsController(
    private val ontologiGateway: LokalOntologiGateway,
    private val styrkCodeConverter: StyrkCodeConverter
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CategoryMapsController::class.java)
        private fun Context.sortParam(): String = queryParam("sort") ?: "code"
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/categories/pyrk/occupations", {
            it.status(HttpStatus.OK).json(getPyrkCategoryMap())
        })

        javalin.get("/api/v1/categories/styrk/occupations", {
            it.status(HttpStatus.OK).json(getStyrkCategoryMap(it.sortParam()))
        })

        javalin.get("/api/v1/categories/janzz/occupations", {
            it.status(HttpStatus.OK).json(getJanzzCategories())
        })
    }

    fun getPyrkCategoryMap(): Map<String, PyrkOccupation> {
        return styrkCodeConverter.occupationMap.toList().distinctBy { (k, v) -> v.categoryLevel2 }
            .sortedBy { (k, v) -> v.styrkCode }.toMap().mapValues { it.value.simplyfy() }
    }

    fun getStyrkCategoryMap(sort: String): Map<String, Occupation> {
        return if ("alfa" == sort) {
            styrkCodeConverter.occupationMap.toList().sortedBy { (k, v) -> v.styrkDescription }.toMap()
        } else styrkCodeConverter.occupationMap.toList().sortedBy { (k, v) -> v.styrkCode }.toMap()
    }

    fun getJanzzCategories(): List<Typeahead> {
        try {
            log.info("Henter stillinger fra ontologi.")
            return ontologiGateway.hentTypeaheadStillingerFraOntologi()
        } catch (e: Exception) {
            log.error("Feilet i henting av typeahead stillinger fra ontologi.", e)
            throw e;
        }
    }

    data class PyrkOccupation(val styrkCode: String, val categoryLevel1: String, val categoryLevel2: String)

    fun Occupation.simplyfy(): PyrkOccupation = PyrkOccupation(styrkCode, categoryLevel1, categoryLevel2)
}
