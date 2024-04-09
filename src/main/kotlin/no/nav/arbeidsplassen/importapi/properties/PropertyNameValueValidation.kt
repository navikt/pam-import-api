package no.nav.arbeidsplassen.importapi.properties

import no.nav.arbeid.pam.kodeverk.ansettelse.*
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.properties.PropertyNames.*
import jakarta.inject.Singleton

@Singleton
class PropertyNameValueValidation {

    private val propertiesToValidate =
        listOf(extent, engagementtype, jobarrangement, workday, workhours, sector, remote, euresflagg)

    private val supportsMultipleValues = listOf(workday, workhours)

    val validValues = HashMap<PropertyNames, Set<String>>()

    init {
        validValues[extent] = Omfang.values().flatMap { it.tekster().values }.toHashSet()
        validValues[engagementtype] = Ansettelsesform.values().flatMap { it.tekster().values }.toHashSet()
        validValues[jobarrangement] = Arbeidstidsordning.values().flatMap { it.tekster().values }.toHashSet()
        validValues[workday] = Arbeidsdager.values().flatMap { it.tekster().values }.toHashSet()
        validValues[workhours] = Arbeidstid.values().flatMap { it.tekster().values }.toHashSet()
        // does not exist in AnsettelseKodeVerk
        validValues[sector] = hashSetOf("Privat", "Offentlig")
        validValues[remote] = hashSetOf("Hjemmekontor", "Hybridkontor", "Hjemmekontor ikke mulig")
        validValues[euresflagg] = hashSetOf("true", "false")
    }

    fun validateProperty(name: PropertyNames, value: String) {
        if (!value.isNullOrEmpty() && !validValues[name]!!.contains(value))
            throw ImportApiError("property $name contains invalid value $value", ErrorType.INVALID_VALUE)
    }

    fun checkOnlyValidValues(properties: Map<PropertyNames, Any>) =
        propertiesToValidate.forEach { propertyToValidate ->
            properties[propertyToValidate]?.let { property ->
                property as String
                if (propertyToValidate in supportsMultipleValues)
                    unwrapStringifiedList(property).forEach { validateProperty(propertyToValidate, it) }
                else
                    validateProperty(propertyToValidate, property)
            }
        }


    private fun unwrapStringifiedList(stringifiedList: String) = stringifiedList
        .removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().removeSurrounding("\"") }
}
