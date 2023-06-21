package no.nav.arbeidsplassen.importapi.ontologi

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

@Singleton
class OntologiGateway(
    @Value("\${pam.ontologi.typeahead.url}") private val baseurl: String,
    private val tokenProvider: LokalTypeaheadTokenProvider
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(OntologiGateway::class.java)
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    fun hentTypeaheadStillingerFraOntologi() : List<Typeahead> {
        val url = "$baseurl/rest/typeahead/stillinger"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 50000
            readTimeout = 50000

            val bearerToken = tokenProvider.token
            setRequestProperty("Authorization", "Bearer $bearerToken")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.use { s -> s.bufferedReader().readText() }
        }
        if (responseCode >= 300 || responseBody == null) {
            log.error("Fikk feil fra samtykke-api: $responseBody")
            throw RuntimeException("Fikk responskode $responseCode fra pam-ontologi og responsmelding $responseBody")
        }

        return responseBody.let {
            val res = mapper.readValue(it, object : TypeReference<List<Typeahead>>() {})
            res ?: listOf()
        }
    }


}

@JsonIgnoreProperties
data class Typeahead(
    val konseptId: Long,
    val styrk08: String,
    val esco: String,
    val label: String
) : Serializable