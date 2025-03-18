package no.nav.arbeidsplassen.importapi.adadminstatus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.kafka.HealthService
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.arbeidsplassen.importapi.kafka.KafkaListenerStarter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalAdTopicListenerTest {


    @Test
    fun `skal kunne starte opp og lytte til Kafka uten feil`() {

        val leaderElection = mock<LeaderElection>()
        `when`(leaderElection.isLeader()).thenReturn(false)
        val healthService = HealthService()
        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        val adminStatusRepository = mock<AdminStatusRepository>()
        val internalAdTopicListener = InternalAdTopicListener(adminStatusRepository, objectMapper)

        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka")).use { container ->
            container.start()

            val kafkaConfig = KafkaConfig(mapOf("KAFKA_BROKERS" to container.bootstrapServers))
            val kafkaListenerStarter = KafkaListenerStarter(
                adTransportProsessor = internalAdTopicListener,
                healthService = healthService,
                kafkaConfig = kafkaConfig,
                leaderElection = leaderElection,
                topic = "teampam.stilling-intern-1",
                groupId = "import-api-adminstatussync-gcp"
            )

            // Testen er bare at den skal kunne starte opp uten feil
            kafkaListenerStarter.start()
        }
    }
}
