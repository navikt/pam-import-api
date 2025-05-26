package no.nav.arbeidsplassen.importapi.kafka

import no.nav.arbeidsplassen.importapi.adadminstatus.InternalAdTopicListener
import no.nav.arbeidsplassen.importapi.leaderelection.LeaderElection
import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.slf4j.LoggerFactory

class KafkaListenerStarter(
    private val adTransportProsessor: InternalAdTopicListener,
    private val healthService: HealthService,
    private val kafkaConfig: KafkaConfig,
    private val leaderElection: LeaderElection,
    private val topic: String,
    private val groupId: String,
    private val adminStatusSyncKafkaEnabled: Boolean
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(KafkaListenerStarter::class.java)
    }

    fun start() {
        // Leader skal ikke lytte på kafkameldinger slik at leader vil overleve en potensiell giftpille
        // og fortsatt kunne håndtere REST-kall
        if (adminStatusSyncKafkaEnabled && !leaderElection.isLeader()) {
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
}
