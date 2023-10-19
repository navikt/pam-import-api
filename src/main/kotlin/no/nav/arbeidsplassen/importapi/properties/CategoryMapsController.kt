package no.nav.arbeidsplassen.importapi.properties

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.Typeahead
import no.nav.pam.yrkeskategorimapper.domain.Occupation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.security.KeyStore
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/api/v1/categories")
class CategoryMapsController(private val ontologiGateway: LokalOntologiGateway) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CategoryMapsController::class.java)
    }

    @Get("/janzz/occupations")
    fun getJanzzCategories(): List<Typeahead> {
        try {
            log.info("Henter stillinger fra ontologi.")
            return ontologiGateway.hentTypeaheadStillingerFraOntologi()
        } catch (e: Exception) {
            log.error("Feilet i henting av typeahead stillinger fra ontologi.", e)
            throw e;
        }
    }

}
