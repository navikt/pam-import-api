package no.nav.arbeidsplassen.importapi.ontologi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.time.Duration
import java.time.LocalDateTime

@Singleton
class LokalTypeaheadTokenProvider(
    @param:Value("\${spring.security.oauth2.client.provider.azure-ad.token-uri}") private val issuerUri: String,
    @param:Value("\${spring.security.oauth2.client.registration.azure-ad.client-id}") private val clientId: String,
    @param:Value("\${spring.security.oauth2.client.registration.azure-ad.client-secret}") private val clientSecret: String,
    @param:Value("\${nais_cluster}") private val naisClusterName: String,
    private val objectMapper: ObjectMapper
) {
    private val httpClient = HttpClient.newBuilder() //.version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(2))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()

    val token: String?
        get() = if (tokenHolder.isValid) tokenHolder.getBearerToken() else try {
            val newToken: BearerToken = renewToken()
            tokenHolder.replaceToken(newToken)
            newToken.access_token
        } catch (e: Exception) {
            LOG.warn("Greide ikke å fornye token: {}", e.message, e)
            null
        }

    fun invalidateToken() {
        try {
            tokenHolder.replaceToken(renewToken())
        } catch (e: Exception) {
            LOG.warn("Failed to invalidate and renew token: {}", e.message, e)
        }
    }

    @Throws(Exception::class)
    fun renewToken(): BearerToken {
        val uri = URI.create(issuerUri)
        val sb = StringBuilder("client_id=")
            .append(URLEncoder.encode(clientId, Charset.forName("UTF-8")))
            .append("&scope=api://").append(naisClusterName).append(".teampam.pam-ontologi/.default")
            .append("&client_secret=").append(URLEncoder.encode(clientSecret, Charset.forName("UTF-8")))
            .append("&grant_type=client_credentials")
        val payload = sb.toString()
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .uri(uri)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        val tokenPayload = response.body()
        return objectMapper.readValue(tokenPayload, BearerToken::class.java)
    }

    internal class TokenHolder {
        private var bearerToken: BearerToken? = null
        private var receivedAt: LocalDateTime? = null
        private var expiresAt: LocalDateTime? = null
        private val skewSeconds: Long = 10
        fun getBearerToken(): String? {
            synchronized(this) { return bearerToken?.access_token }
        }

        fun replaceToken(token: BearerToken?) {
            synchronized(this) {
                var now = LocalDateTime.now()
                bearerToken = token
                receivedAt = now
                expiresAt = now.plusSeconds(java.lang.Long.valueOf(bearerToken?.expires_in ?: "0"))
            }
        }

        val isValid: Boolean
            get() = bearerToken != null && receivedAt != null && expiresAt != null && expiresAt!!.minusSeconds(
                skewSeconds
            ).isAfter(LocalDateTime.now())
    }

    companion object {
        private val tokenHolder = TokenHolder()
        private val LOG = LoggerFactory.getLogger(LokalTypeaheadTokenProvider::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class BearerToken {
    var expires_in: String? = null
    var access_token: String? = null
}