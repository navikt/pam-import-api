package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Sort
import io.micronaut.transaction.annotation.TransactionalEventListener
import no.nav.arbeidsplassen.importapi.Open
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.adstate.AdstateKafkaSender
import no.nav.arbeidsplassen.importapi.dto.*
import no.nav.arbeidsplassen.importapi.properties.PropertyType
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.transaction.Transactional.TxType
import kotlin.streams.toList

@Singleton
@Open
class TransferLogTasks(private val transferLogRepository: TransferLogRepository,
                       private val adStateRepository: AdStateRepository,
                       private val objectMapper: ObjectMapper,
                       private val meterRegistry: MeterRegistry,
                       private val kafkaSender: AdstateKafkaSender,
                       private val eventPublisher: ApplicationEventPublisher,
                       @Value("\${transferlog.adstate.kafka.enabled}") private val adStateKafkaSend: Boolean,
                       @Value("\${transferlog.tasks-size:50}") private val logSize: Int,
                       @Value("\${transferlog.delete.months:6}") private val deleteMonths: Long) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogTasks::class.java)
    }


    fun processTransferLogTask():Int {
        val transferlogs = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED, Pageable.from(0,logSize, Sort.of(Sort.Order.asc("updated"))))
        transferlogs.stream().forEach {
            mapTransferLog(it)
        }
        return transferlogs.count()
    }

    fun deleteTransferLogTask(date: LocalDateTime = LocalDateTime.now().minusMonths(deleteMonths)) {
        LOG.info("Deleting transferlog before $date")
        transferLogRepository.deleteByUpdatedBefore(date)
    }

    @Transactional
    fun mapTransferLog(it: TransferLog) {
        try {
            val adList = mapTransferLogs(it)
            LOG.info("mapping transfer ${it.id} for provider ${it.providerId} found ${adList.size} ads ")
            val savedList = adStateRepository.saveAll(adList)
            transferLogRepository.save(it.copy(status = TransferLogStatus.DONE))
            meterRegistry.counter("ads_received", "provider", it.providerId.toString()).increment(adList.size.toDouble())
            eventPublisher.publishEvent(AdStateEvent(savedList, it.providerId))
        } catch (e: Exception) {
            LOG.error("Got exception while handling transfer log ${it.id}", e)
            transferLogRepository.save(it.copy(status = TransferLogStatus.ERROR))
        }
    }

    private fun mapTransferLogs(transferLog: TransferLog): List<AdState> {
        val ads = objectMapper.readValue(transferLog.payload, object: TypeReference<List<AdDTO>>(){})
        return ads.stream().map { mapAdToAdState(it, transferLog)}.toList()

    }

    private fun mapAdToAdState(ad: AdDTO, transferLog: TransferLog): AdState {
        val inDb = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb.map {
            it.copy(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(htmlSanitizeAd(ad))) }
                .orElseGet{ AdState(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(htmlSanitizeAd(ad)),
                        providerId = transferLog.providerId, reference = ad.reference)}
    }

    private fun htmlSanitizeAd(ad: AdDTO): AdDTO {
        val text = sanitize(ad.adText)
        val props = ad.properties.map { (key, value) ->
            when (key.type) {
                PropertyType.HTML -> key to sanitize(value.toString())
                else -> key to value
            }
        }.toMap()
        return ad.copy(adText = text, properties = props, categoryList = ad.categoryList.distinct())
    }

    @TransactionalEventListener
    fun onNewAdEvent(event: AdStateEvent) {
        if (adStateKafkaSend) {
            LOG.info("sending batch of ${event.adList.count()} adstates for provider ${event.providerId}")
            kafkaSender.send(event.adList).subscribe(
                    { LOG.info("Successfully sent to kafka adstates with uuid ${event.adList.map { it.uuid + " " }}") },
                    { LOG.error("Got error while sending to kafka adstates with uuid: ${event.adList.map { it.uuid + " " }}", it) }
            )
        }
    }

    data class AdStateEvent(val adList: Iterable<AdState>, val providerId: Long)
}

