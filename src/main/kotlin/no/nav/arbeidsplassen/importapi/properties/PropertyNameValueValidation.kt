package no.nav.arbeidsplassen.importapi.properties

import no.nav.arbeid.pam.kodeverk.ansettelse.*
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import javax.inject.Singleton

@Singleton
class PropertyNameValueValidation {

    private val EXTENT = "extent"
    private val ENGAGEMENTTYPE = "engagementtype"
    private val JOBARRANGEMENT = "jobarrangement"
    private val WORKDAY = "workday"
    private val WORKHOURS = "workhours"
    private val SECTOR = "sector"
    private val propertiesToValidate = listOf(EXTENT, ENGAGEMENTTYPE,JOBARRANGEMENT,
            WORKDAY, WORKHOURS, SECTOR)

    val validValues = HashMap<String,Set<String>>()

    init {
        validValues[EXTENT] = Omfang.values().flatMap{ it.tekster().values }.toHashSet()
        validValues[ENGAGEMENTTYPE] = Ansettelsesform.values().flatMap{ it.tekster().values }.toHashSet()
        validValues[JOBARRANGEMENT] = Arbeidstidsordning.values().flatMap { it.tekster().values }.toHashSet()
        validValues[WORKDAY] = Arbeidsdager.values().flatMap { it.tekster().values }.toHashSet()
        validValues[WORKHOURS] = Arbeidstid.values().flatMap { it.tekster().values }.toHashSet()
        // does not exist in AnsettelseKodeVerk
        validValues[SECTOR] = hashSetOf("Privat", "Offentlig")

    }

    fun validateProperty(name:String, value: String) {
        if (!validValues[name]!!.contains(value))
            throw ImportApiError("property $name contains invalid value $value", ErrorType.INVALID_VALUE)
    }

    fun checkOnlyValidValues(properties: HashMap<String, Any>) {
        propertiesToValidate.forEach {
            if (properties.containsKey(it))
                validateProperty(it, properties[it] as String)
        }
    }


}
