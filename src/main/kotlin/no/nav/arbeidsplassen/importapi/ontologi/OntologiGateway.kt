package no.nav.arbeidsplassen.importapi.ontologi

interface OntologiGateway {

    fun hentTypeaheadStillingerFraOntologi(): List<Typeahead>
    
    fun hentTypeaheadStilling(stillingstittel: String): List<Typeahead>

    fun hentStyrkOgEscoKonsepterBasertPaJanzz(konseptId: Long): KonseptGrupperingDTO?
}
