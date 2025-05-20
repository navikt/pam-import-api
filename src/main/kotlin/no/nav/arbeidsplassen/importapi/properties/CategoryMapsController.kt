package no.nav.arbeidsplassen.importapi.properties

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CategoryMapsController(
    private val ontologiGateway: LokalOntologiGateway,
    private val styrkCodeConverter: StyrkCodeConverter
) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(CategoryMapsController::class.java)
        private fun Context.sortParam(): String = queryParam("sort") ?: "code"
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get("/api/v1/categories/pyrk/occupations", { getPyrkCategoryMap(it) })
        javalin.get("/api/v1/categories/styrk/occupations", { getStyrkCategoryMap(it) })

        javalin.get("/api/v1/categories/janzz/occupations", { getJanzzCategories(it) })
    }

    fun getPyrkCategoryMap(ctx: Context) {
        //TODO  IntelliJ ga meg egentlig Map<String?, PyrkOccupation> ..
        val pyrkCategoryMap: Map<String, PyrkOccupation> = styrkCodeConverter.occupationMap
            .toList()
            .distinctBy { (_, v) -> v.categoryLevel2 }
            .sortedBy { (_, v) -> v.styrkCode }
            .toMap()
            .mapValues { it.value.simplyfy() }
        ctx.status(HttpStatus.OK).json(pyrkCategoryMap)
    }

    fun getStyrkCategoryMap(ctx: Context) {
        val sort = ctx.sortParam()
        val styrkCategoryMap = if ("alfa" == sort) {
            styrkCodeConverter.occupationMap
                .toList()
                .sortedBy { (_, v) -> v.styrkDescription }
                .toMap()
        } else {
            styrkCodeConverter.occupationMap
                .toList()
                .sortedBy { (_, v) -> v.styrkCode }
                .toMap()
        }
        ctx.status(HttpStatus.OK).json(styrkCategoryMap)
    }

    fun getJanzzCategories(ctx: Context) {
        try {
            LOG.info("Henter stillinger fra ontologi.")
            val typeahead = ontologiGateway.hentTypeaheadStillingerFraOntologi()
            ctx.status(HttpStatus.OK).json(typeahead)
        } catch (e: Exception) {
            LOG.error("Feilet i henting av typeahead stillinger fra ontologi.", e)
            throw e;
        }
    }

    data class PyrkOccupation(val styrkCode: String, val categoryLevel1: String, val categoryLevel2: String)

    fun Occupation.simplyfy(): PyrkOccupation = PyrkOccupation(styrkCode, categoryLevel1, categoryLevel2)
}
