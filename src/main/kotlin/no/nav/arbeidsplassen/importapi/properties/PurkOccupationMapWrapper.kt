package no.nav.arbeidsplassen.importapi.properties

import no.nav.arbeidsplassen.importapi.properties.CategoryMapsController.PyrkOccupation

data class PurkOccupationMapWrapper(private val map: Map<String, PyrkOccupation>) : Map<String, PyrkOccupation> by map
