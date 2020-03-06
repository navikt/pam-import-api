package no.nav.arbeidsplassen.importapi.dto

import java.time.LocalDateTime

data class AdDTO(val reference: String, val published: LocalDateTime?, val expires: LocalDateTime?,
                 val contactList: List<ContactDTO> = listOf(), val locationList: List<LocationDTO> = listOf(),
                 val properties: Map<PropertyNames, Any> = hashMapOf(), val title: String, val adText: String,
                 val privacy: PrivacyType = PrivacyType.SHOW_ALL, val positions: Int = 1,
                 val employer: EmployerDTO?, val categoryList: List<CategoryDTO> = listOf())

data class EmployerDTO(val reference: String, val businessName: String, val orgnr: String?, val location: LocationDTO)

data class CategoryDTO(val code: String, val categoryType: CategoryType, val name: String?, val description: String?)

data class ContactDTO(val name: String?, val title: String?, val email: String?, val phone: String?, val role: String?)

data class LocationDTO(val address: String?, val postalCode: String?, val country: String?,
                       val county: String?, val municipal: String?, val city: String?,
                       val latitude: String?, val longitude: String?)

enum class CategoryType {
    STYRK08, PYRK20
}

enum class PrivacyType {
    SHOW_ALL, INTERNAL_NOT_SHOWN, DONT_SHOW_EMPLOYER, DONT_SHOW_AUTHOR
}