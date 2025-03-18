package no.nav.arbeidsplassen.importapi.adadminstatus

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.feed.AdTransport
import no.nav.arbeidsplassen.importapi.kafka.KafkaRapidJsonListener
import org.slf4j.LoggerFactory


@Singleton
class InternalAdTopicListener(
    private val adminStatusRepository: AdminStatusRepository,
    private val jacksonMapper: ObjectMapper
) : KafkaRapidJsonListener.RapidMessageListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(InternalAdTopicListener::class.java)
    }

    override fun onMessage(message: KafkaRapidJsonListener.JsonMessage) {
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

    /*
    @Topic("\${adminstatus.kafka.topic:teampam.stilling-intern-1}")
    fun kakfkaAdminStatusSyncWithAd(adList: List<AdTransport>, offsets: List<Long>) {
        LOG.info("received from kafka with batch size of {} ads", adList.size)
        val adminList = adList.stream()
            .filter { "IMPORTAPI" == it.source }
            .map {
                LOG.info("Mapping import api ad ${it.uuid}")
                it.toAdminStatus(adminStatusRepository)
            }
            .toList()

        if (adminList.isNotEmpty()) {
            val distinctList = adminList.sortedByDescending(AdminStatus::updated).distinctBy(AdminStatus::uuid)
            adminStatusRepository.saveAll(distinctList)
            LOG.info("{} was saved as import-api ads ", distinctList.size)
        }
        LOG.info("committing latest offset {} with ad {}", offsets.last(), adList.last().uuid)
    }
     */
}
