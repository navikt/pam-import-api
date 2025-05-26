package no.nav.arbeidsplassen.importapi.ontologi

interface OntologiGateway {

    fun hentTypeaheadStillingerFraOntologi(): List<Typeahead>

    // TODO: Trenger en test! Jeg fjerner bruken av Micronauts UriTemplate, må verifiseres
    fun hentTypeaheadStilling(stillingstittel: String): List<Typeahead>

    fun hentStyrkOgEscoKonsepterBasertPaJanzz(konseptId: Long): KonseptGrupperingDTO?
}
