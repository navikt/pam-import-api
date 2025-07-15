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

    lateinit var listenerThread: Thread

    fun start() {
        // Når kun leader lytter på kafkameldinger vil systemet som en helhet overleve en potensiell giftpille
        // og fortsatt kunne håndtere REST-kall
        if (adminStatusSyncKafkaEnabled && leaderElection.isLeader()) {
            LOG.info("Starter kafka rapid listener")
            try {
                val consumerConfig = kafkaConfig.kafkaJsonConsumer(topic, groupId)
                val listener = KafkaTopicJsonListener(consumerConfig, healthService, adTransportProsessor)
                listenerThread = listener.startListener()
            } catch (e: Exception) {
                LOG.error("Greide ikke å starte Kafka listener: ${e.message}", e)
                healthService.addUnhealthyVote()
            }
        } else {
            LOG.info("Starter IKKE kafka rapid listener; adminStatusSyncKafkaEnabled=$adminStatusSyncKafkaEnabled og leaderElection=${leaderElection.isLeader()}")
        }
    }

    fun stop() {
        if (adminStatusSyncKafkaEnabled && leaderElection.isLeader()) {
            LOG.info("Stopper kafka rapid listener")
            try {
                listenerThread.interrupt()
            } catch (e: Exception) {
                LOG.error("Greide ikke å stoppe Kafka listener: ${e.message}", e)
                healthService.addUnhealthyVote()
            }
        }
    }
}
