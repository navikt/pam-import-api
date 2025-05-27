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

class TestSecretSignatureConfigProperties() : SecretSignatureConfigProperties(
    secret = "Thisisaverylongsecretandcanonlybeusedintest",
)

abstract class DatabaseConfigProperties(
    val host: String,
    val port: String,
    val database: String,
    val user: String,
    val pw: String,
    val dbDriver: String,
    // private val url :String,
    val additionalParameter: String,
)

class NaisDatabaseConfigProperties(env: Map<String, String>) : DatabaseConfigProperties(
    host = env.variable("DB_HOST"),
    port = env.variable("DB_PORT"),
    database = env.variable("DB_DATABASE"),
    user = env.variable("DB_USERNAME"),
    pw = env.variable("DB_PASSWORD"),
    dbDriver = env.variable("DB_DRIVER"),
    // url = env.variable("DB_URL"),
    additionalParameter = env["DB_ADDITIONAL_PARAMETER"] ?: ""
)

class TestDatabaseConfigProperties(
    host: String, port: String
) : DatabaseConfigProperties(
    host = host,
    port = port,
    database = "test",
    user = "test",
    pw = "test",
    dbDriver = "org.postgresql.Driver",
    // url = env.variable("DB_URL"),
    additionalParameter = ""
) {
    constructor(env: Map<String, String>) :
            this(host = env.variable("DB_HOST"), port = env.variable("DB_PORT"))
}

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

class TestKafkaConfigProperties(
    brokers: String
) : KafkaConfigProperties(
    brokers = brokers,
    credstorePassword = null,
    truststorePath = null,
    keystorePath = null,
    applicationName = null
) {
    constructor(env: Map<String, String>) : this(env.variable("KAFKA_BROKERS"))
}

abstract class OutgoingPortsConfigProperties(
    val ontologiBaseUrl: String,
    val electorPath: String,
)

class NaisOutgoingPortsConfigProperties(env: Map<String, String>) : OutgoingPortsConfigProperties(
    ontologiBaseUrl = "http://pam-ontologi",
    electorPath = env.variable("ELECTOR_PATH"),
)

class TestOutgoingPortsConfigProperties(
    ontologiBaseUrl: String = "http://pam-ontologi", // TODO Denne vil vel aldri være i bruk ?
    electorPath: String = "NOLEADERELECTION",
) : OutgoingPortsConfigProperties(
    ontologiBaseUrl = ontologiBaseUrl,
    electorPath = electorPath,
)

abstract class ControllerConfigProperties(
    val adPreviewUrl: String,
    val transferlogBatchSize: Int = 100,
)

class NaisControllerConfigProperties(env: Map<String, String>) : ControllerConfigProperties(
    adPreviewUrl = env.variable("AD_PREVIEW_URL"),
    transferlogBatchSize = 100
)

class TestControllerConfigProperties(
    adPreviewUrl: String = "http://localhost:8080/stillinger/intern",
    transferlogBatchSize: Int = 100,
) : ControllerConfigProperties(
    adPreviewUrl = adPreviewUrl,
    transferlogBatchSize = transferlogBatchSize
)

abstract class ServicesConfigProperties(
    val adminStatusPreviewUrl: String,
    val adminStatusTopic: String,
    val adminStatusGroupId: String,
    val adminStatusSyncKafkaEnabled: Boolean,
    val adOutboxKafkaProducerTopic: String,
    val logSize: Int = 500, // TODO Er dette transferlog.task-size ?
    val transferlogDeleteMonths: Long = 6
)

class NaisServicesConfigProperties(env: Map<String, String>) : ServicesConfigProperties(
    adminStatusPreviewUrl = env.variable("AD_PREVIEW_URL"),
    adminStatusTopic = env.variable("ADMINSTATUS_KAFKA_TOPIC"),
    adminStatusGroupId = "import-api-adminstatussync-gcp",
    adminStatusSyncKafkaEnabled = true,
    adOutboxKafkaProducerTopic = env.variable("ANNONSEMOTTAK_TOPIC"),
)

class TestServicesConfigProperties(
    adminStatusPreviewUrl: String = "http://localhost:8080/stillinger/intern",
    adminStatusTopic: String = "teampam.stilling-intern-1",
    adminStatusGroupId: String = "import-api-adminstatussync-gcp",
    adminStatusSyncKafkaEnabled: Boolean = false,
    adOutboxKafkaProducerTopic: String = "teampam.annonsemottak-1"
) : ServicesConfigProperties(
    adminStatusPreviewUrl = adminStatusPreviewUrl,
    adminStatusTopic = adminStatusTopic,
    adminStatusGroupId = adminStatusGroupId,
    adminStatusSyncKafkaEnabled = adminStatusSyncKafkaEnabled,
    adOutboxKafkaProducerTopic = adOutboxKafkaProducerTopic,
)

abstract class SchedulerConfigProperties(
    val adOutboxJobEnabled: Boolean,
    val transferlogJobEnabled: Boolean,
)

class NaisSchedulerConfigProperties(env: Map<String, String>) : SchedulerConfigProperties(
    adOutboxJobEnabled = env.variable("AD_OUTBOX_SCHEDULER_ENABLED").toBoolean(),
    // Denne mangler i oppsettet, så her vil defaulten bli brukt:
    transferlogJobEnabled = env.nullableVariable("AD_OUTBOX_SCHEDULER_ENABLED")?.toBoolean() ?: true,
)

class TestSchedulerConfigProperties(
    adOutboxJobEnabled: Boolean = false,
    transferlogJobEnabled: Boolean = false
) : SchedulerConfigProperties(
    adOutboxJobEnabled = adOutboxJobEnabled,
    transferlogJobEnabled = transferlogJobEnabled,
)
