package no.nav.arbeidsplassen.importapi.transfer

import java.time.LocalDateTime

data class Transfer(val provider: Provider, val ads: List<Ad> = listOf())

data class Ad(val reference: String, val created: LocalDateTime = LocalDateTime.now(),
              val published: LocalDateTime = LocalDateTime.now(), val expires: LocalDateTime,
              val updated: LocalDateTime = LocalDateTime.now(), val contactList: List<Contact> = listOf(),
              val locationList: List<Location> = listOf(), val properties: Map<String, Any> = hashMapOf(),
              val title: String, val status: String = "ACTIVE", val privacy: String, val positions: Int = 1,
              val employer: Employer?, val categoryList: List<Category> = listOf())

data class Provider(val id: Long?, val uuid: String?, val userName: String?)

data class Employer(val reference: String, val businessName: String, val orgnr: String?)

data class Category(val code: String, val categoryType: String = "AP2020", val name: String?, val description: String?)

data class Contact(val name: String?, val title: String?, val email: String?, val phone: String?, val role: String?)

data class Location(val address: String?, val postalCode: String?, val country: String?,
                       val county: String?, val municipal: String?, val city: String?,
                       val latitude: String?, val longitude: String?)

