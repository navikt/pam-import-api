package no.nav.arbeidsplassen.importapi.adadminstatus

import io.micronaut.configuration.kafka.ConsumerAware
import io.micronaut.configuration.kafka.annotation.*
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.core.convert.format.Format
import no.nav.arbeidsplassen.importapi.feed.AdTransport
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.stream.Collector
import java.util.stream.Collectors
import kotlin.streams.toList


@Requires(property = "adminstatussync.kafka.enabled", value="true")
@KafkaListener(groupId="\${adminstatus.kafka.group-id:import-api-adminstatussync-gcp}", threads = 1, offsetReset = OffsetReset.EARLIEST,
        batch = true, offsetStrategy = OffsetStrategy.SYNC)
class AdminStatusSyncWithKafka(private val adminStatusRepository: AdminStatusRepository,
                               @Value("\${adminstatussync.kafka.offsettimestamp}")
                               @Format("yyyy-MM-dd'T'HH:mm:ss'Z'") private val offsetTimeStamp: LocalDateTime?): ConsumerRebalanceListener, ConsumerAware<Any,Any> {

    private lateinit var consumer: Consumer<Any,Any>

    companion object {
        private val LOG = LoggerFactory.getLogger(AdminStatusSyncWithKafka::class.java)
    }

    @Topic("\${adminstatus.kafka.topic:StillingIntern}")
    fun kakfkaAdminStatusSyncWithAd(adList: List<AdTransport>, offsets: List<Long>) {
        LOG.info("received from kafka with batch size of {} ads", adList.size)
        val adminList = adList.stream()
                .filter{ "IMPORTAPI" == it.source }
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

    override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>) {
        if (offsetTimeStamp!=null) {
            LOG.info("Resetting offset for timestamp {}", offsetTimeStamp)
            val topicPartitionTimestamp = partitions.map { it to offsetTimeStamp.toMillis() }.toMap()
            val partitionOffsetMap = consumer.offsetsForTimes(topicPartitionTimestamp)
            partitionOffsetMap.forEach { (topic, timestamp) -> consumer.seek(topic, timestamp.offset()) }
        }
    }

    override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>?) {
        LOG.info("onPartionsRevoked is called")
    }

    override fun setKafkaConsumer(consumer: Consumer<Any, Any>) {
        this.consumer = consumer
    }
}

fun LocalDateTime.toMillis(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
