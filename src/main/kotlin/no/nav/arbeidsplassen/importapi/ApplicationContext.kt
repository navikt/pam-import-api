package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import java.net.http.HttpClient
import java.util.TimeZone
import no.nav.arbeidsplassen.importapi.common.Singeltons
import no.nav.arbeidsplassen.importapi.config.DatabaseConfig
import no.nav.arbeidsplassen.importapi.config.lesEnvVarEllerKastFeil
import no.nav.arbeidsplassen.importapi.kafka.KafkaListenerStarter
import no.nav.arbeidsplassen.importapi.nais.HealthService
import no.nav.arbeidsplassen.importapi.nais.NaisController
import no.nav.arbeidsplassen.importapi.provider.JdbcProviderRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderController
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.security.TokenService
import no.nav.arbeidsplassen.importapi.transferlog.TransferController

@Suppress("MemberVisibilityCanBePrivate")
open class ApplicationContext(envInn: Map<String, String>) {

    val env: Map<String, String> = envInn

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))

    val prometheusRegistry = Singeltons.meterRegistry.also { registry ->
        ClassLoaderMetrics().bindTo(registry)
        JvmMemoryMetrics().bindTo(registry)
        JvmGcMetrics().bindTo(registry)
        JvmThreadMetrics().bindTo(registry)
        UptimeMetrics().bindTo(registry)
        ProcessorMetrics().bindTo(registry)
    }

    val dataSource = DatabaseConfig(env, prometheusRegistry.prometheusRegistry).lagDatasource()

    val txTemplate = TxTemplate(dataSource)

    val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    val jwtSecret = env.lesEnvVarEllerKastFeil("SECRET") // "Thisisaverylongsecretandcanonlybeusedintest"

    val tokenVerifier: JWSVerifier = MACVerifier(jwtSecret)
    val tokenService: TokenService = TokenService(jwtSecret)
    val healthService = HealthService()

    val naisController = NaisController(healthService, prometheusRegistry)

    open val providerRepository: ProviderRepository = JdbcProviderRepository(txTemplate)
    val providerService by lazy { ProviderService(providerRepository) }
    val providerController by lazy {
        ProviderController(providerService, tokenService)
    }

    val transferController: TransferController = TODO()

    val kafkaListenerStarter: KafkaListenerStarter = TODO()

}
