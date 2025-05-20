package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import java.net.http.HttpClient
import java.util.TimeZone
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusController
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusService
import no.nav.arbeidsplassen.importapi.adadminstatus.JdbcAdminStatusRepository
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxRepository
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxService
import no.nav.arbeidsplassen.importapi.adoutbox.JdbcAdOutboxRepository
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsController
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsRepository
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsService
import no.nav.arbeidsplassen.importapi.adpuls.JdbcAdPulsRepository
import no.nav.arbeidsplassen.importapi.adstate.AdPreviewController
import no.nav.arbeidsplassen.importapi.adstate.AdStateController
import no.nav.arbeidsplassen.importapi.adstate.AdStateInternalController
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.adstate.JdbcAdStateRepository
import no.nav.arbeidsplassen.importapi.common.Singeltons
import no.nav.arbeidsplassen.importapi.config.DatabaseConfig
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfiguration
import no.nav.arbeidsplassen.importapi.config.lesEnvVarEllerKastFeil
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.arbeidsplassen.importapi.kafka.KafkaListenerStarter
import no.nav.arbeidsplassen.importapi.nais.HealthService
import no.nav.arbeidsplassen.importapi.nais.NaisController
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.provider.JdbcProviderRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderCache
import no.nav.arbeidsplassen.importapi.provider.ProviderController
import no.nav.arbeidsplassen.importapi.provider.ProviderRepository
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.arbeidsplassen.importapi.security.TokenService
import no.nav.arbeidsplassen.importapi.transferlog.JdbcTransferLogRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferController
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogRepository
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogService
import org.apache.kafka.clients.producer.KafkaProducer

@Suppress("MemberVisibilityCanBePrivate")
open class ApplicationContext(envInn: Map<String, String>) {

    val env: Map<String, String> = envInn

    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))

    val prometheusRegistry = Singeltons.meterRegistry.also { registry ->
        ClassLoaderMetrics().bindTo(registry)
        JvmMemoryMetrics().bindTo(registry)
        JvmGcMetrics().bindTo(registry)
        JvmThreadMetrics().bindTo(registry)
        UptimeMetrics().bindTo(registry)
        ProcessorMetrics().bindTo(registry)
    }

    val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    val ontologiGateway: LokalOntologiGateway = LokalOntologiGateway(
        baseurl = TODO()
    )

    val kafkaConfig = KafkaConfig(env)

    val secretConfiguration =
        SecretSignatureConfiguration(env.lesEnvVarEllerKastFeil("SECRET")) // // "Thisisaverylongsecretandcanonlybeusedintest"

    val dataSource = DatabaseConfig(env, prometheusRegistry.prometheusRegistry).lagDatasource()

    val txTemplate = TxTemplate(dataSource)

    // Repositories
    open val providerRepository: ProviderRepository = JdbcProviderRepository(txTemplate)
    val transferLogRepository: TransferLogRepository = JdbcTransferLogRepository(txTemplate)
    val adStatRepository: AdStateRepository = JdbcAdStateRepository(txTemplate)
    val adminStateRepository: AdminStatusRepository = JdbcAdminStatusRepository(txTemplate)
    val adPulsRepository: AdPulsRepository = JdbcAdPulsRepository(txTemplate)
    val adOutboxRepository: AdOutboxRepository = JdbcAdOutboxRepository(txTemplate)

    // Services
    val healthService = HealthService()

    // val tokenVerifier: JWSVerifier = MACVerifier(jwtSecret)
    val providerCache = ProviderCache()
    val providerService by lazy { ProviderService(providerRepository, providerCache) }
    val propertyNameValueValidation: PropertyNameValueValidation = PropertyNameValueValidation()
    val transferLogService: TransferLogService = TransferLogService(
        transferLogRepository = transferLogRepository,
        propertyNameValueValidation = propertyNameValueValidation,
        ontologiGateway = ontologiGateway
    )
    val adStateService: AdStateService = AdStateService(
        adStateRepository = adStatRepository,
        objectMapper = objectMapper,
        providerService = providerService
    )
    val adminStatusService: AdminStatusService = AdminStatusService(
        adminStatusRepository = adminStateRepository,
        previewUrl = TODO()
    )
    val adPulsService: AdPulsService = AdPulsService(adPulsRepository)
    val kafkaProducer: KafkaProducer<String, ByteArray?> = kafkaConfig.kafkaProducer()
    val adOutboxKafkaProducer: AdOutboxKafkaProducer = AdOutboxKafkaProducer(
        kafkaProducer = kafkaProducer,
        topic = TODO(),
        healthService = healthService
    )
    val adOutboxService: AdOutboxService = AdOutboxService(
        adOutboxKafkaProducer = TODO(),
        adOutboxRepository = adOutboxRepository,
        adStateService = adStateService,
        objectMapper = objectMapper
    )
    val tokenService: TokenService = TokenService(secretConfiguration)
    val adTransportProcessor

    // Controllers
    val naisController = NaisController(healthService, prometheusRegistry, secretConfiguration)
    val providerController by lazy { ProviderController(providerService, tokenService) }
    val transferController: TransferController = TransferController(
        transferLogService = transferLogService,
        providerService = providerService,
        adStateService = adStateService,
        objectMapper = objectMapper,
        adsSize = TODO()
    )
    val adStateController: AdStateController = AdStateController(adStateService)
    val adStateInternalController: AdStateInternalController = AdStateInternalController(adStateService)
    val adPreviewController: AdPreviewController = AdPreviewController(
        adStateService = adStateService,
        previewUrl = TODO()
    )
    val adminStatusController: AdminStatusController = AdminStatusController(adminStatusService)
    val adPulsController: AdPulsController = AdPulsController(adPulsService)

    // Kafka Listeners
    val kafkaListenerStarter: KafkaListenerStarter = KafkaListenerStarter(
        adTransportProsessor = TODO(),
        healthService = TODO(),
        kafkaConfig = TODO(),
        leaderElection = TODO(),
        topic = TODO(),
        groupId = TODO()
    )

    // @Requires(property = "adoutbox.kafka.enabled", value = "true")
    fun adOutboxProducer(
        topic: String, // TODO @Value("\${adoutbox.kafka.topic:teampam.annonsemottak-1}")
        healthService: HealthService,
        kafkaConfig: KafkaConfig
    ): AdOutboxKafkaProducer = TODO()


}
