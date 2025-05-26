package no.nav.arbeidsplassen.importapi.config

class ApplicationProperties {
}

class ApplicationProperty<T>(
    val value: T,
)

class NullableApplicationProperty<T>(
    val value: T?,
)

class ApplicationStringProperty
interface ApplicationProperties {
    interface TransferlogProperties {
        fun batchSize(): Int {
            throw FeilOppsettException()
        }

        fun taskSize(): Int {
            throw FeilOppsettException()
        }

        fun monthsToDelete(): Int {
            throw FeilOppsettException()
        }

        fun schedulerEnabled(): Boolean {
            throw FeilOppsettException()
        }
    }

    interface AdOutboxProperties {
        fun kafkaEnabled(): Boolean {
            throw FeilOppsettException()
        }

        fun kafkaTopic(): String {
            throw FeilOppsettException()
        }

        fun schedulerEnabled(): Boolean {
            throw FeilOppsettException()
        }
    }

    interface AdminStatusProperties {
        fun kafkaEnabled(): Boolean {
            throw FeilOppsettException()
        }
    }

    interface KafkaProperties {
        fun brokers(): String {
            throw FeilOppsettException()
        }

        fun credstorePassword(): String? {}
    }

    class FeilOppsettException() : IllegalStateException("Property not defined")
}
