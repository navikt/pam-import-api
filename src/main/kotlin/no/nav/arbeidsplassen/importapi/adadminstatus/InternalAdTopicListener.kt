package no.nav.arbeidsplassen.importapi.adadminstatus

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.arbeidsplassen.importapi.feed.AdTransport
import no.nav.arbeidsplassen.importapi.kafka.KafkaTopicJsonListener
import org.slf4j.LoggerFactory


class InternalAdTopicListener(
    private val adminStatusRepository: AdminStatusRepository,
    private val jacksonMapper: ObjectMapper
) : KafkaTopicJsonListener.TopicMessageListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(InternalAdTopicListener::class.java)
    }

    override fun onMessage(message: KafkaTopicJsonListener.JsonMessage) {
        if (message.payload != null) {
            try {
                val adTransport = jacksonMapper.readValue<AdTransport>(message.payload)
                if (adTransport.source == "IMPORTAPI") {
                    LOG.info("Mapping import api ad ${adTransport.uuid}")
                    val adminStatus = adTransport.toAdminStatus(adminStatusRepository)
                    adminStatusRepository.save(adminStatus)
                    LOG.info("{} was saved as import-api ad", adminStatus.uuid)
                }
            } catch (e: Exception) {
                LOG.error("Greide ikke å konsumere/mappe AdTransport med key ${message.key}: ${e.message}", e)
                throw (e)
            }
        }
    }
}
