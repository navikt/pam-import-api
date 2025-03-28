package no.nav.arbeidsplassen.importapi.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdDTO(
    val reference: String,
    val published: LocalDateTime?,
    val expires: LocalDateTime?,
    val contactList: List<ContactDTO> = listOf(),
    val locationList: List<LocationDTO> = listOf(),
    val properties: Map<PropertyNames, String> = hashMapOf(),
    val title: String,
    val adText: String,
    val status: AdStatus = AdStatus.RECEIVED,
    val privacy: PrivacyType = PrivacyType.SHOW_ALL,
    val positions: Int = 1,
    val employer: EmployerDTO?,
    val categoryList: List<CategoryDTO> = listOf()
) {
    init {
        require(reference.isNotBlank() && reference.length<255) {"reference is blank or size > 255"}
        require(title.isNotBlank() ) {"title should not be blank"}
        require(title.length < 512) {"title should not have size > 512 (was ${title.length})"}
        require(locationList.isNotEmpty() || employer?.orgnr != null) {"LocationList is empty, please specify at least one Location"}
        require(adText.isNotBlank()) {"adtext is blank"}
        require(positions > 0 ) {"positions should be 1 or more"}
        require(employer != null) { "Employer should not be empty" }
    }
}

data class EmployerDTO(val reference: String?, val businessName: String, var orgnr: String?, val location: LocationDTO) {
    init {
        if (orgnr.isNullOrEmpty()) {
            require(businessName.isNotBlank() && businessName.length < 255) { "businessName is blank or size > 255" }
        }
        if (orgnr!=null && orgnr!!.contains("\\s".toRegex())) {
            // Strip all spaces, pam-ad requires this
            orgnr = orgnr!!.replace("\\s".toRegex(),"")
        }
    }
}

data class CategoryDTO(val code: String, val categoryType: CategoryType = CategoryType.JANZZ, val name: String? = null, var janzzParentId: String? = null) {
}

data class ContactDTO(val name: String?, val title: String?, val email: String?, val phone: String?, val role: String?) {

    init {
        require(name.isNullOrBlank() || name.length<255) {"Contact name size > 255"}
        require(title.isNullOrBlank() || title.length<255) {"Contact title size > 255"}
        require(email.isNullOrBlank() || email.length<255) {"Contact email size > 255"}
        require(phone.isNullOrBlank() || phone.length<36) {"Contact phone size > 36"}
        require(role.isNullOrBlank() || role.length<255) {"Contact role is > 255 "}
    }
}

data class LocationDTO(val address: String?=null, val postalCode: String?=null, val country: String?="Norge",
                       val county: String?=null, val municipal: String?=null, val city: String?=null,
                       val latitude: String?=null, val longitude: String?=null) {

     fun isCountryAbroad() = !country.isNullOrEmpty() && !listOf("NORGE", "NOREG", "NORWAY", "NO").contains(country.uppercase())
}

enum class CategoryType {
    STYRK08, PYRK20, JANZZ, ESCO
}

enum class PrivacyType {
   @JsonEnumDefaultValue SHOW_ALL, INTERNAL_NOT_SHOWN, @Deprecated("not_supported") DONT_SHOW_EMPLOYER
}

enum class AdStatus {
    RECEIVED, STOPPED, DELETED
}

