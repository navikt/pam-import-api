package no.nav.arbeidsplassen.importapi.config

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusService
import no.nav.arbeidsplassen.importapi.adadminstatus.InternalAdTopicListener
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxKafkaProducer
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxScheduler
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxService
import no.nav.arbeidsplassen.importapi.adoutbox.KafkaAdOutboxSendAndGet
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsService
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsTasks
import no.nav.arbeidsplassen.importapi.adstate.AdStateService
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.arbeidsplassen.importapi.kafka.KafkaListenerStarter
import no.nav.arbeidsplassen.importapi.nais.HealthService
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.provider.ProviderCache
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import no.nav.arbeidsplassen.importapi.security.JavalinAccessManager
import no.nav.arbeidsplassen.importapi.security.TokenService
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogScheduler
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogService
import no.nav.arbeidsplassen.importapi.transferlog.TransferLogTasks
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import org.slf4j.LoggerFactory

class ServicesApplicationContext(
    servicesConfigurationProperties: ServicesConfigurationProperties,
    databaseApplicationContext: DatabaseApplicationContext,
    baseServicesApplicationContext: BaseServicesApplicationContext,
    outgoingPortsApplicationContext: OutgoingPortsApplicationContext,
    kafkaConfig: KafkaConfig,
    secretSignatureConfigProperties: SecretSignatureConfigProperties

) : OnServerStartup {

    companion object {
        private val LOG = LoggerFactory.getLogger(ServicesApplicationContext::class.java)
    }

    val healthService = HealthService()
    val providerCache = ProviderCache()
    val providerService by lazy {
        ProviderService(
            providerRepository = databaseApplicationContext.providerRepository,
            providerCache = providerCache
        )
    }
    val propertyNameValueValidation: PropertyNameValueValidation = PropertyNameValueValidation()
    val transferLogService: TransferLogService = TransferLogService(
        transferLogRepository = databaseApplicationContext.transferLogRepository,
        propertyNameValueValidation = propertyNameValueValidation,
        ontologiGateway = outgoingPortsApplicationContext.ontologiGateway
    )
    val adStateService: AdStateService = AdStateService(
        adStateRepository = databaseApplicationContext.adStatRepository,
        objectMapper = baseServicesApplicationContext.objectMapper,
        providerService = providerService
    )
    val adminStatusService: AdminStatusService = AdminStatusService(
        adminStatusRepository = databaseApplicationContext.adminStatusRepository,
        previewUrl = servicesConfigurationProperties.adminStatusPreviewUrl
    )
    val adPulsService: AdPulsService = AdPulsService(databaseApplicationContext.adPulsRepository)
    val synchronousKafkaSendAndGet = KafkaAdOutboxSendAndGet(kafkaConfig.kafkaProducer())
    val adOutboxKafkaProducer: AdOutboxKafkaProducer = AdOutboxKafkaProducer(
        synchronousKafkaSendAndGet = synchronousKafkaSendAndGet,
        topic = servicesConfigurationProperties.adOutboxKafkaProducerTopic,
        healthService = healthService
    )
    val adOutboxService: AdOutboxService = AdOutboxService(
        adOutboxKafkaProducer = adOutboxKafkaProducer,
        adOutboxRepository = databaseApplicationContext.adOutboxRepository,
        adStateService = adStateService,
        objectMapper = baseServicesApplicationContext.objectMapper
    )
    val tokenService: TokenService = TokenService(secretSignatureConfigProperties)
    val tokenVerifier: JWSVerifier = MACVerifier(secretSignatureConfigProperties.secret)
    val accessManager = JavalinAccessManager(
        providerService = providerService,
        verifier = tokenVerifier,
    )

    val internalAdTopicListener: InternalAdTopicListener = InternalAdTopicListener(
        adminStatusRepository = databaseApplicationContext.adminStatusRepository,
        jacksonMapper = baseServicesApplicationContext.objectMapper
    )

    val kafkaListenerStarter: KafkaListenerStarter = KafkaListenerStarter(
        adTransportProsessor = internalAdTopicListener,
        healthService = healthService,
        kafkaConfig = kafkaConfig,
        leaderElection = outgoingPortsApplicationContext.leaderElection,
        topic = servicesConfigurationProperties.adminStatusTopic,
        groupId = servicesConfigurationProperties.adminStatusGroupId,
        adminStatusSyncKafkaEnabled = servicesConfigurationProperties.adminStatusSyncKafkaEnabled
    )

    //Scheduler services:

    val adOutboxScheduler = AdOutboxScheduler(
        adOutboxService = adOutboxService,
        leaderElection = outgoingPortsApplicationContext.leaderElection
    )
    val adPulsTasks = AdPulsTasks(
        adPulsRepository = databaseApplicationContext.adPulsRepository,
        leaderElection = outgoingPortsApplicationContext.leaderElection
    )
    val styrkCodeConverter = StyrkCodeConverter()
    val transferLogTasks = TransferLogTasks(
        transferLogRepository = databaseApplicationContext.transferLogRepository,
        adStateRepository = databaseApplicationContext.adStatRepository,
        objectMapper = baseServicesApplicationContext.objectMapper,
        meterRegistry = baseServicesApplicationContext.prometheusRegistry,
        styrkCodeConverter = styrkCodeConverter,
        lokalOntologiGateway = outgoingPortsApplicationContext.ontologiGateway,
        adOutboxService = adOutboxService,
        txTemplate = databaseApplicationContext.txTemplate,
        logSize = servicesConfigurationProperties.logSize,
        deleteMonths = servicesConfigurationProperties.deleteMonths
    )
    val transferLogScheduler = TransferLogScheduler(
        transferLogTasks = transferLogTasks,
        leaderElection = outgoingPortsApplicationContext.leaderElection
    )

    override fun onServerStartup() {
        LOG.info("onApplicationEvent StartupEvent")
        kafkaListenerStarter.start()
    }
}
