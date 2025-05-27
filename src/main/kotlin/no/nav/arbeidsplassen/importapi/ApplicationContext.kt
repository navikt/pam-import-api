package no.nav.arbeidsplassen.importapi

import no.nav.arbeidsplassen.importapi.config.ApplicationProperties
import no.nav.arbeidsplassen.importapi.config.BaseServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.ControllerApplicationContext
import no.nav.arbeidsplassen.importapi.config.ControllerConfigProperties
import no.nav.arbeidsplassen.importapi.config.DatabaseApplicationContext
import no.nav.arbeidsplassen.importapi.config.DatabaseConfigProperties
import no.nav.arbeidsplassen.importapi.config.DefaultOutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.config.KafkaConfigProperties
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsApplicationContext
import no.nav.arbeidsplassen.importapi.config.OutgoingPortsConfigProperties
import no.nav.arbeidsplassen.importapi.config.SchedulerApplicationContext
import no.nav.arbeidsplassen.importapi.config.SchedulerConfigProperties
import no.nav.arbeidsplassen.importapi.config.SecretSignatureConfigProperties
import no.nav.arbeidsplassen.importapi.config.SecurityServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.ServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.ServicesConfigProperties
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig

open class ApplicationContext(applicationProperties: ApplicationProperties) {

    //Properties:
    open val secretSignatureConfigProperties: SecretSignatureConfigProperties =
        applicationProperties.secretSignatureConfigProperties

    open val outgoingPortsConfigurationProperties: OutgoingPortsConfigProperties =
        applicationProperties.outgoingPortsConfigProperties

    open val servicesConfigurationProperties: ServicesConfigProperties =
        applicationProperties.servicesConfigProperties

    open val databaseConfigurationProperties: DatabaseConfigProperties =
        applicationProperties.databaseConfigProperties

    open val controllerConfigProperties: ControllerConfigProperties =
        applicationProperties.controllerConfigProperties

    open val schedulerConfigurationProperties: SchedulerConfigProperties =
        applicationProperties.schedulerConfigProperties

    open val kafkaConfigProperties: KafkaConfigProperties =
        applicationProperties.kafkaConfigProperties

    //Brukes flere steder:
    open val baseServicesApplicationContext by lazy { BaseServicesApplicationContext() }
    open val kafkaConfig by lazy { KafkaConfig(kafkaConfigProperties) }
    open val outgoingPortsApplicationContext: OutgoingPortsApplicationContext by lazy {
        DefaultOutgoingPortsApplicationContext(
            kafkaConfig = kafkaConfig,
            outgoingPortsConfigProperties = outgoingPortsConfigurationProperties,
            baseServicesApplicationContext = baseServicesApplicationContext
        )
    }

    // Database og repositories:
    open val databaseApplicationContext by lazy {
        DatabaseApplicationContext(
            databaseConfigProperties = databaseConfigurationProperties,
            baseServicesApplicationContext = baseServicesApplicationContext
        )
    }

    // Services
    open val securityServicesApplicationContext by lazy {
        SecurityServicesApplicationContext(
            secretSignatureConfigProperties = secretSignatureConfigProperties,
            databaseApplicationContext = databaseApplicationContext,
        )
    }

    open val servicesApplicationContext by lazy {
        ServicesApplicationContext(
            servicesConfigProperties = servicesConfigurationProperties,
            databaseApplicationContext = databaseApplicationContext,
            baseServicesApplicationContext = baseServicesApplicationContext,
            outgoingPortsApplicationContext = outgoingPortsApplicationContext,
            kafkaConfig = kafkaConfig,
            securityServicesApplicationContext = securityServicesApplicationContext,
        )
    }

    // Controllers
    open val controllerApplicationContext by lazy {
        ControllerApplicationContext(
            secretSignatureConfigProperties = secretSignatureConfigProperties,
            controllerConfigProperties = controllerConfigProperties,
            baseServicesApplicationContext = baseServicesApplicationContext,
            servicesApplicationContext = servicesApplicationContext,
            securityServicesApplicationContext = securityServicesApplicationContext,
        )
    }

    // Scheduler:
    open val schedulerApplicationContext by lazy {
        SchedulerApplicationContext(
            schedulerConfigProperties = schedulerConfigurationProperties,
            servicesApplicationContext = servicesApplicationContext
        )
    }
}
