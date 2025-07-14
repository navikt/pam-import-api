package no.nav.arbeidsplassen.importapi.ontologi

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class LokalOntologiGateway(
    private val baseurl: String,
) : OntologiGateway {

    companion object {
        val log: Logger = LoggerFactory.getLogger(LokalOntologiGateway::class.java)
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    override fun hentTypeaheadStillingerFraOntologi(): List<Typeahead> {
        val url = "$baseurl/rest/typeahead/stillinger"
        val (responseCode, responseBody) = with(URI(url).toURL().openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 50000
            readTimeout = 50000

            setRequestProperty("Nav-CallId", UUID.randomUUID().toString())
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-type", "application/json; charset=utf-8")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.use { s -> s.bufferedReader().readText() }
        }
        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("Fikk responskode $responseCode fra pam-ontologi og responsmelding $responseBody")
        }

        return responseBody.let {
            val res = mapper.readValue(it, object : TypeReference<List<Typeahead>>() {})
            res ?: listOf()
        }
    }

    override fun hentTypeaheadStilling(stillingstittel: String): List<Typeahead> {
        val encodedPath = URLEncoder.encode(stillingstittel, StandardCharsets.UTF_8.toString())
        val url = "$baseurl/rest/typeahead/stilling?stillingstittel=${encodedPath}"
        
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .header("Nav-CallId", UUID.randomUUID().toString())
            .build();
        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return jacksonObjectMapper().readValue(response.body(), object : TypeReference<List<Typeahead>>() {})
    }

    override fun hentStyrkOgEscoKonsepterBasertPaJanzz(konseptId: Long): KonseptGrupperingDTO? {
        val uri = URI("$baseurl/rest/ontologi/konseptGruppering/$konseptId")

        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(uri)
            .header("Nav-CallId", UUID.randomUUID().toString())
            .build();
        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return jacksonObjectMapper().readValue(response.body(), object : TypeReference<KonseptGrupperingDTO>() {})
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class KonseptDTO(
    val konseptId: Long,
    val type: String,
    val noLabel: String,
    val enLabel: String,
    val nnLabel: String,
    val styrk08SSB: List<String>,
    val esco: List<String>,
    val umbrella: Boolean,
    val noDescription: String,
    val enDescription: String,
    val termer: List<TermDTO>
)

data class TermDTO(
    val id: Int,
    val konseptId: Long,
    val tag: String,
    val spraak: String,
    val verdi: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KonseptGrupperingDTO(
    val konseptId: Long,
    val noLabel: String?,
    val styrk08SSB: List<String>,
    val esco: EscoDTO?
)

data class EscoDTO(
    val label: String?,
    val uri: String
)
