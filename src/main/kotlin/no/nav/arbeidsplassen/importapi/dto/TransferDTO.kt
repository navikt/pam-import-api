package no.nav.arbeidsplassen.importapi.dto

import java.time.LocalDateTime

data class Transfer(val provider: Provider, val ads: List<Ad>)

data class Provider(val id: Long?, val uuid: String, val userName: String?, val email: String?)

data class Ad(val reference: String, val published: LocalDateTime?, val expires: LocalDateTime?,
              val contactList: List<Contact> = listOf(), val locationList: List<Location> = listOf(),
              val properties: Map<String, Any> = hashMapOf(),
              val title: String, val privacy: String, val positions: Int = 1,
              val employer: Employer?, val categoryList: List<Category> = listOf())

data class Employer(val reference: String, val businessName: String, val orgnr: String?)

data class Category(val code: String, val categoryType: String = "AP2020", val name: String?, val description: String?)

data class Contact(val name: String?, val title: String?, val email: String?, val phone: String?, val role: String?)

data class Location(val address: String?, val postalCode: String?, val country: String?,
                       val county: String?, val municipal: String?, val city: String?,
                       val latitude: String?, val longitude: String?)

