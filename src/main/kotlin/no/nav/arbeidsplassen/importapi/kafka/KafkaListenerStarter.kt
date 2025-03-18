package no.nav.arbeidsplassen.importapi.kafka

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.adadminstatus.InternalAdTopicListener
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "adminstatussync.kafka.enabled", value = "true")
class KafkaListenerStarter(
    private val adTransportProsessor: InternalAdTopicListener,
    private val healthService: HealthService,
    private val kafkaConfig: KafkaConfig,
    private val leaderElection: LeaderElection,
    @Value("\${adminstatus.kafka.topic:teampam.stilling-intern-1}") private val topic: String,
    @Value("\${adminstatus.kafka.group-id:import-api-adminstatussync-gcp}") private val groupId: String,
) : ApplicationEventListener<StartupEvent> {

    companion object {
        private val LOG = LoggerFactory.getLogger(KafkaListenerStarter::class.java)
    }

    fun start() {
        // Leader skal ikke lytte på kafkameldinger slik at leader vil overleve en potensiell giftpille
        // og fortsatt kunne håndtere REST-kall
        if (!leaderElection.isLeader()) {
            LOG.info("Starter kafka rapid listener")
            try {
                val consumerConfig = kafkaConfig.kafkaJsonConsumer(topic, groupId)
                val listener = KafkaRapidJsonListener(consumerConfig, adTransportProsessor, healthService)
                listener.startListener()
            } catch (e: Exception) {
                LOG.error("Greide ikke å starte kafka consumer: ${e.message}", e)
            }
        }
    }

    override fun onApplicationEvent(event: StartupEvent?) {
        start()
    }
}
