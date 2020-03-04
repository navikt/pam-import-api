package no.nav.arbeidsplassen.importapi.feed

import java.time.LocalDateTime
import java.util.*


data class AdTransport(val id: Long, val uuid: UUID, val createdBy: String, val updatedBy: String, val created: LocalDateTime,
                       val updated: LocalDateTime, val contactList: List<Contact> = listOf(), val mediaList: List<Media> = listOf(),
                       val locationList: List<Location> = listOf(), val properties: Map<String, String> = hashMapOf(),
                       val title: String, val status: String, val privacy: String, val source: String,
                       val medium: String, val reference: String, val published: LocalDateTime?, val expires: LocalDateTime,
                       val employer: AdCompany?, val categoryList: List<Category> = listOf(), val businessName: String?,
                       val administration: Administration, val publishedByAdmin: LocalDateTime?)

data class Category(val id: Long, val code: String, val categoryType: String, val name: String, val description: String?, val parentId: Long?)

data class Contact(val name: String?, val title: String?, val email: String?,
                   val phone: String?, val role: String?)

data class Media(val name: String?, val type: String?, val reference: String?)

data class Location(val address: String?, val postalCode: String?, val country: String?,
                    val county: String?, val municipal: String?, val city: String?,
                    val latitude: String?, val longitude: String? )

data class AdCompany(val id: Long, val uuid: UUID, val createdBy: String, val updatedBy: String, val created: LocalDateTime,
                     val updated: LocalDateTime, val contactList: List<Contact> = listOf(), val mediaList: List<Media> = listOf(),
                     val locationList: List<Location> = listOf(), val properties: Map<String, String> = hashMapOf(),
                     val name: String, val orgnr: String?, val status: String, val parentOrgnr: String?,
                     val publicName: String?, val deactivated: LocalDateTime?, val orgform: String?, val employees: Int)

data class Administration(val status: String, val comments: String?, val reportee: String?,
                          val remarks :List<String> = listOf(), val navIdent: String?)
