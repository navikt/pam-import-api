package no.nav.arbeidsplassen.importapi.dto

import no.nav.arbeid.pam.kodeverk.ansettelse.*
import javax.inject.Singleton

@Singleton
class PropertyNameValueValidation {

    private val validValues = HashMap<String,Set<String>>()
    private val EXTENT = "extent"
    private val ENGAGEMENTTYPE = "engagementtype"
    private val JOBARRANGEMENT = "jobarrangement"
    private val WORKDAY = "workday"
    private val WORKHOURS = "workhours"
    private val SECTOR = "sector"
    private val propertiesToValidate = listOf<String>(EXTENT, ENGAGEMENTTYPE,JOBARRANGEMENT,
            WORKDAY, WORKHOURS, SECTOR)

    init {
        validValues[EXTENT] = Omfang.values().flatMap{ it.tekster().values }.toHashSet()
        validValues[ENGAGEMENTTYPE] = Ansettelsesform.values().flatMap{ it.tekster().values }.toHashSet()
        validValues[JOBARRANGEMENT] = Arbeidstidsordning.values().flatMap { it.tekster().values }.toHashSet()
        validValues[WORKDAY] = Arbeidsdager.values().flatMap { it.tekster().values }.toHashSet()
        validValues[WORKHOURS] = Arbeidstid.values().flatMap { it.tekster().values }.toHashSet()
        // does not exist in AnsettelseKodeVerk
        validValues[SECTOR] = hashSetOf("Privat, Offentlig")

    }

    fun validateProperty(name:String, value: String) {
        if (!validValues.get(name)!!.contains(value))
            throw ApiError("property $name contains invalid value $value", ErrorType.INVALID_VALUE)
    }

    fun checkOnlyValidValues(properties: HashMap<String, Any>) {
        propertiesToValidate.forEach {
            if (properties.containsKey("extent"))
                validateProperty("extent", properties["extent"] as String)
        }
    }

}