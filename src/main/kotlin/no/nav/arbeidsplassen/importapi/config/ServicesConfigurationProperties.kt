package no.nav.arbeidsplassen.importapi.config

open class ServicesConfigurationProperties(
    val adminStatusPreviewUrl: String,
    val adminStatusTopic: String,
    val adminStatusGroupId: String,
    val adminStatusSyncKafkaEnabled: Boolean,
    val adOutboxKafkaProducerTopic: String,
    val logSize: Int,
    val deleteMonths: Long
) {
    companion object {
        fun ServicesConfigurationProperties(env: Map<String, String>): ServicesConfigurationProperties =
            ServicesConfigurationProperties(
                adminStatusPreviewUrl = env.variable("ad.preview.url"),
                adminStatusTopic = env.nullableVariable("adminstatus.kafka.topic") ?: "teampam.stilling-intern-1",
                adminStatusGroupId = env.nullableVariable("adminstatus.kafka.group-id")
                    ?: "import-api-adminstatussync-gcp",
                adminStatusSyncKafkaEnabled = env.nullableVariable("adminstatussync.kafka.enabled")?.toBoolean()
                    ?: false,
                adOutboxKafkaProducerTopic = env.nullableVariable("adoutbox.kafka.topic") ?: "teampam.annonsemottak-1",
                logSize = env.nullableVariable("transferlog.tasks-size")?.toInt() ?: 50,
                deleteMonths = env.nullableVariable("transferlog.delete.months")?.toLong() ?: 6,
            )
    }

}
