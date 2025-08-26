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

                    // Fix for NPE caused by uknown bug in pam-ad, skipping this ad
                    if (setOf(
                            "731557db-c5f3-4493-89ae-cf8ec4bdec85",
                            "16b64093-14bb-4afc-8568-e2420bb21e90",
                            "6083d0ab-899f-41a6-84a0-4d343686851b",
                            "455230ba-a5b5-4367-8056-742817e4a877",
                        ).contains(message.key)
                    ) {
                        return
                    }

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
