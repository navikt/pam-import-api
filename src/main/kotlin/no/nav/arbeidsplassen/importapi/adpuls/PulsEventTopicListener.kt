package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.OffsetStrategy
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusRepository
import org.slf4j.LoggerFactory

@Requires(property = "pulsevent.kafka.enabled", value="true")
@KafkaListener(groupId="\${pulsevent.kafka.group-id:import-api-pulsevent}", threads = 1, offsetReset = OffsetReset.LATEST,
    batch = true, offsetStrategy = OffsetStrategy.SYNC)
class PulsEventTopicListener(private val adInfoService: AdPulsService, private val adminStatusRepository: AdminStatusRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(PulsEventTopicListener::class.java)
    }

    @Topic("\${pulsevent.kafka.topic:teampam.puls-intern-2}")
    fun syncPulsEvents(events: List<PulsEventDTO>, offsets: List<Long>) {
        LOG.info("Received ${events.size} events from puls")
        val dtos = adInfoService.saveAll(
            events.filter { PulsEventType.fromValue(it.type) != PulsEventType.unknown }
            .map { it.toAdInfoDTO() }
            .filterNotNull()
        )
        LOG.info("${dtos.size} events was saved")
    }

    fun PulsEventDTO.toAdInfoDTO(): AdPulsDTO? {
       return adminStatusRepository.findByUuid(this.oid)?.let {
           AdPulsDTO(
               id = it.id,
               uuid = it.uuid,
               type = PulsEventType.fromValue(this.type),
               total = this.total,
               providerId = it.providerId,
               reference = it.reference
           )
       }
    }
}

