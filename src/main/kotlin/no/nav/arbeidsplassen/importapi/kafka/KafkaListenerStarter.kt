package no.nav.arbeidsplassen.importapi.kafka

import no.nav.arbeidsplassen.importapi.LeaderElection
import no.nav.arbeidsplassen.importapi.adadminstatus.InternalAdTopicListener
import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.slf4j.LoggerFactory

// TODO @Requires(property = "adminstatussync.kafka.enabled", value = "true")
class KafkaListenerStarter(
    private val adTransportProsessor: InternalAdTopicListener,
    private val healthService: HealthService,
    private val kafkaConfig: KafkaConfig,
    private val leaderElection: LeaderElection,
    private val topic: String, // TODO @Value("\${adminstatus.kafka.topic:teampam.stilling-intern-1}")
    private val groupId: String, // TODO @Value("\${adminstatus.kafka.group-id:import-api-adminstatussync-gcp}")
) {

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
                val listener = KafkaTopicJsonListener(consumerConfig, healthService, adTransportProsessor)
                listener.startListener()
            } catch (e: Exception) {
                LOG.error("Greide ikke å starte Kafka listener: ${e.message}", e)
                healthService.addUnhealthyVote()
            }
        }
    }

    fun onServerStartup() {
        LOG.info("onApplicationEvent StartupEvent")
        start()
    }
}
