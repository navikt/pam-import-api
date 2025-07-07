package no.nav.arbeidsplassen.importapi.config

class TestSecretSignatureConfigProperties() : SecretSignatureConfigProperties(
    secret = "Thisisaverylongsecretandcanonlybeusedintest",
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

class TestOutgoingPortsConfigProperties(
    ontologiBaseUrl: String = "http://pam-ontologi", // TODO Denne vil vel aldri være i bruk ?
    electorPath: String = "NOLEADERELECTION",
) : OutgoingPortsConfigProperties(
    ontologiBaseUrl = ontologiBaseUrl,
    electorPath = electorPath,
)

class TestControllerConfigProperties(
    adPreviewUrl: String = "http://localhost:8080/stillinger/intern",
    transferlogBatchSize: Int = 100,
) : ControllerConfigProperties(
    adPreviewUrl = adPreviewUrl,
    transferlogBatchSize = transferlogBatchSize
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

class TestSchedulerConfigProperties(
    adOutboxJobEnabled: Boolean = false,
    transferlogJobEnabled: Boolean = false
) : SchedulerConfigProperties(
    adOutboxJobEnabled = adOutboxJobEnabled,
    transferlogJobEnabled = transferlogJobEnabled,
)
