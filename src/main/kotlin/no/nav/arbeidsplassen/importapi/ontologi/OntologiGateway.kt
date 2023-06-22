package no.nav.arbeidsplassen.importapi.ontologi

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Singleton
class OntologiGateway(
    @Value("\${pam.ontologi.typeahead.url}") private val baseurl: String,
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(OntologiGateway::class.java)
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    fun hentTypeaheadStillingerFraOntologi() : Map<String, Typeahead> {
        val url = "$baseurl/rest/typeahead/stillinger"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 50000
            readTimeout = 50000

            setRequestProperty("Nav-CallId", UUID.randomUUID().toString())
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.use { s -> s.bufferedReader().readText() }
        }
        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("Fikk responskode $responseCode fra pam-ontologi og responsmelding $responseBody")
        }

        return responseBody.let {
            val res = mapper.readValue(it, object : TypeReference<List<Typeahead>>() {})
            res?.sortedBy { typeahead ->  typeahead.code}?.associate { typeahead -> typeahead.name to typeahead } ?: mapOf()
        }
    }


}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Typeahead(
    @JsonAlias("konseptId")
    val code: Long,
    val categoryType: String = "JANZZ",
    @JsonAlias("label")
    val name: String
) : Serializable