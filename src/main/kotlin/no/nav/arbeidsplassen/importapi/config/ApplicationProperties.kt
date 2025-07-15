package no.nav.arbeidsplassen.importapi.config

data class ApplicationProperties(
    val secretSignatureConfigProperties: SecretSignatureConfigProperties,
    val databaseConfigProperties: DatabaseConfigProperties,
    val kafkaConfigProperties: KafkaConfigProperties,
    val outgoingPortsConfigProperties: OutgoingPortsConfigProperties,
    val servicesConfigProperties: ServicesConfigProperties,
    val controllerConfigProperties: ControllerConfigProperties,
    val schedulerConfigProperties: SchedulerConfigProperties
) {
    companion object {
        fun NaisApplicationProperties(env: Map<String, String>) = ApplicationProperties(
            secretSignatureConfigProperties = NaisSecretSignatureConfigProperties(env),
            databaseConfigProperties = NaisDatabaseConfigProperties(env),
            kafkaConfigProperties = NaisKafkaConfigProperties(env),
            outgoingPortsConfigProperties = NaisOutgoingPortsConfigProperties(env),
            servicesConfigProperties = NaisServicesConfigProperties(env),
            controllerConfigProperties = NaisControllerConfigProperties(env),
            schedulerConfigProperties = NaisSchedulerConfigProperties(env)
        )
    }
}

abstract class SecretSignatureConfigProperties(
    val secret: String
)

class NaisSecretSignatureConfigProperties(env: Map<String, String>) : SecretSignatureConfigProperties(
    secret = env.variable("JWT_SECRET")
)

abstract class DatabaseConfigProperties(
    val host: String,
    val port: String,
    val database: String,
    val user: String,
    val pw: String,
    val dbDriver: String,
    val additionalParameter: String,
)

class NaisDatabaseConfigProperties(env: Map<String, String>) : DatabaseConfigProperties(
    host = env.variable("DB_HOST"),
    port = env.variable("DB_PORT"),
    database = env.variable("DB_DATABASE"),
    user = env.variable("DB_USERNAME"),
    pw = env.variable("DB_PASSWORD"),
    dbDriver = env.variable("DB_DRIVER"),
    additionalParameter = env["DB_ADDITIONAL_PARAMETER"] ?: ""
)


abstract class KafkaConfigProperties(
    val brokers: String,
    val credstorePassword: String?,
    val truststorePath: String?,
    val keystorePath: String?,
    val applicationName: String?
)

class NaisKafkaConfigProperties(env: Map<String, String>) : KafkaConfigProperties(
    brokers = env.variable("KAFKA_BROKERS"),
    credstorePassword = env.nullableVariable("KAFKA_CREDSTORE_PASSWORD"),
    truststorePath = env.nullableVariable("KAFKA_TRUSTSTORE_PATH"),
    keystorePath = env.nullableVariable("KAFKA_KEYSTORE_PATH"),
    applicationName = env.nullableVariable("NAIS_APP_NAME")
)

abstract class OutgoingPortsConfigProperties(
    val ontologiBaseUrl: String,
    val electorPath: String,
)

class NaisOutgoingPortsConfigProperties(env: Map<String, String>) : OutgoingPortsConfigProperties(
    ontologiBaseUrl = "http://pam-ontologi",
    electorPath = env.variable("ELECTOR_PATH"),
)

abstract class ControllerConfigProperties(
    val adPreviewUrl: String,
    val transferlogBatchSize: Int,
)

class NaisControllerConfigProperties(env: Map<String, String>) : ControllerConfigProperties(
    adPreviewUrl = env.variable("AD_PREVIEW_URL"),
    transferlogBatchSize = 100
)

abstract class ServicesConfigProperties(
    val adminStatusPreviewUrl: String,
    val adminStatusTopic: String,
    val adminStatusGroupId: String,
    val adminStatusSyncKafkaEnabled: Boolean,
    val adOutboxKafkaProducerTopic: String,
    val transferlogDeleteMonths: Long,
)

class NaisServicesConfigProperties(env: Map<String, String>) : ServicesConfigProperties(
    adminStatusPreviewUrl = env.variable("AD_PREVIEW_URL"),
    adminStatusTopic = env.variable("ADMINSTATUS_KAFKA_TOPIC"),
    adminStatusGroupId = "import-api-adminstatussync-gcp",
    adminStatusSyncKafkaEnabled = true,
    adOutboxKafkaProducerTopic = env.variable("ANNONSEMOTTAK_TOPIC"),
    transferlogDeleteMonths = 6
)

abstract class SchedulerConfigProperties(
    val adOutboxJobEnabled: Boolean,
    val transferlogJobEnabled: Boolean,
)

class NaisSchedulerConfigProperties(env: Map<String, String>) : SchedulerConfigProperties(
    adOutboxJobEnabled = env.variable("AD_OUTBOX_SCHEDULER_ENABLED").toBoolean(),
    transferlogJobEnabled = true,
)
