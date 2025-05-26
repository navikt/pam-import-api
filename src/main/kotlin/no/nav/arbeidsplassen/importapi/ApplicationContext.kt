package no.nav.arbeidsplassen.importapi

import no.nav.arbeidsplassen.importapi.config.BaseServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.ControllerApplicationContext
import no.nav.arbeidsplassen.importapi.config.ControllerConfigProperties.Companion.ControllerConfigProperties
import no.nav.arbeidsplassen.importapi.config.DatabaseApplicationContext
import no.nav.arbeidsplassen.importapi.config.DatabaseConfigProperties.Companion.DatabaseConfigProperties
import no.nav.arbeidsplassen.importapi.config.DefaultOutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsConfigurationProperties.Companion.OutgoingPortsConfigurationProperties
import no.nav.arbeidsplassen.importapi.config.SchedulerApplicationContext
import no.nav.arbeidsplassen.importapi.config.SchedulerConfigurationProperties.Companion.SchedulerConfigurationProperties
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfigProperties.Companion.SecretSignatureConfigProperties
import no.nav.arbeidsplassen.importapi.config.ServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.ServicesConfigurationProperties.Companion.ServicesConfigurationProperties
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig

open class ApplicationContext(envInn: Map<String, String>) {

    private val env: Map<String, String> = envInn

    //Properties:
    open val secretSignatureConfigProperties = SecretSignatureConfigProperties(env)
    open val outgoingPortsConfigurationProperties = OutgoingPortsConfigurationProperties(env)
    open val servicesConfigurationProperties = ServicesConfigurationProperties(env)
    open val databaseConfigurationProperties = DatabaseConfigProperties(env)
    open val controllerConfigProperties = ControllerConfigProperties(env)
    open val schedulerConfigurationProperties = SchedulerConfigurationProperties(env)

    //Brukes flere steder:
    open val baseServicesApplicationContext = BaseServicesApplicationContext()
    open val kafkaConfig = KafkaConfig(env)
    open val outgoingPortsApplicationContext: OutgoingPortsApplicationContext = DefaultOutgoingPortsApplicationContext(
        kafkaConfig = kafkaConfig,
        outgoingPortsConfigurationProperties = outgoingPortsConfigurationProperties,
        baseServicesApplicationContext = baseServicesApplicationContext
    )

    // Database og repositories:
    open val databaseApplicationContext = DatabaseApplicationContext(
        databaseConfigurationProperties,
        collectorRegistry = baseServicesApplicationContext.prometheusRegistry.prometheusRegistry
    )

    // Services

    open val servicesApplicationContext = ServicesApplicationContext(
        servicesConfigurationProperties = servicesConfigurationProperties,
        databaseApplicationContext = databaseApplicationContext,
        baseServicesApplicationContext = baseServicesApplicationContext,
        outgoingPortsApplicationContext = outgoingPortsApplicationContext,
        kafkaConfig = kafkaConfig,
        secretSignatureConfigProperties = secretSignatureConfigProperties,
    )

    // Controllers
    open val controllerApplicationContext = ControllerApplicationContext(
        secretSignatureConfigProperties = secretSignatureConfigProperties,
        controllerConfigProperties = controllerConfigProperties,
        baseServicesApplicationContext = baseServicesApplicationContext,
        servicesApplicationContext = servicesApplicationContext,
    )

    // Scheduler:
    open val schedulerApplicationContext = SchedulerApplicationContext(
        schedulerConfigurationProperties = schedulerConfigurationProperties,
        servicesApplicationContext = servicesApplicationContext
    )
}
