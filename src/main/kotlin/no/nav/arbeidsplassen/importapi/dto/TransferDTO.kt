package no.nav.arbeidsplassen.importapi.dto

import java.time.LocalDateTime

data class TransferDTO(val provider: ProviderDTO, val ads: List<AdDTO>)

data class ProviderDTO(val id: Long?, val uuid: String, val userName: String?, val email: String?)

data class AdDTO(val reference: String, val published: LocalDateTime?, val expires: LocalDateTime?,
                 val contactList: List<ContactDTO> = listOf(), val locationList: List<LocationDTO> = listOf(),
                 val properties: Map<String, Any> = hashMapOf(),
                 val title: String, val adText: String, val privacy: String = "SHOW_ALL", val positions: Int = 1,
                 val employer: EmployerDTO?, val categoryList: List<CategoryDTO> = listOf())

data class EmployerDTO(val reference: String, val businessName: String, val orgnr: String?, val location: LocationDTO)

data class CategoryDTO(val code: String, val categoryType: String = "AP2020", val name: String?, val description: String?)

data class ContactDTO(val name: String?, val title: String?, val email: String?, val phone: String?, val role: String?)

data class LocationDTO(val address: String?, val postalCode: String?, val country: String?,
                       val county: String?, val municipal: String?, val city: String?,
                       val latitude: String?, val longitude: String?)

