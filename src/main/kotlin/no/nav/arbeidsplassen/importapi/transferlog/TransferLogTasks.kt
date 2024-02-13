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
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.ontologi.KonseptGrupperingDTO
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import javax.transaction.Transactional

@Singleton
@Open
class TransferLogTasks(private val transferLogRepository: TransferLogRepository,
                       private val adStateRepository: AdStateRepository,
                       private val objectMapper: ObjectMapper,
                       private val meterRegistry: MeterRegistry,
                       private val kafkaSender: AdstateKafkaSender,
                       private val eventPublisher: ApplicationEventPublisher<AdStateEvent>,
                       private val styrkCodeConverter : StyrkCodeConverter,
                       private val lokalOntologiGateway : LokalOntologiGateway,
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
        LOG.info("Mapping ad {} for providerId {} transferlog {}", ad.reference, transferLog.providerId, transferLog.id)
        val inDb = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb.map {
            it.copy(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(sanitizeAd(ad))) }
                .orElseGet{ AdState(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(sanitizeAd(ad)),
                        providerId = transferLog.providerId, reference = ad.reference)}
    }

    fun sanitizeAd(ad: AdDTO): AdDTO {
        val props = ad.properties.map { (key, value) ->
            when (key.type) {
                PropertyType.HTML -> key to sanitize(value)
                else -> key to value
            }
        }.toMutableList()

        val categoryGroupList = ad.categoryGroupList.toMutableList()

        ad.categoryList.forEach { janzzCategory ->
            try {
                val categoryGroup = CategoryGroupDTO(mutableListOf(janzzCategory))
                val konseptgruppering: KonseptGrupperingDTO? =
                    lokalOntologiGateway.hentStyrkOgEscoKonsepterBasertPaJanzz(janzzCategory.code.toLong())
                if (konseptgruppering != null) {
                    addEscoToCategoriesIfExists(categoryGroup, konseptgruppering)
                    addStyrkToCategoriesIfExists(categoryGroup, konseptgruppering)
                }
                categoryGroupList.add(categoryGroup)
            } catch (e: Exception) {
                LOG.warn("Feilet i kall mot pam-ontologi for reference {}", ad.reference, e)
            }
        }
        val returnAd = ad.copy(
            adText = sanitize(ad.adText),
            title = ad.title.replaceAmpersand(),
            employer = ad.employer?.copy(
                businessName = ad.employer.businessName.replaceAmpersand()
            ),
            categoryGroupList = categoryGroupList,
            properties = props.toMap(),
        )
        LOG.info(returnAd.categoryList.toString())
        return returnAd
    }

    private fun addEscoToCategoriesIfExists(
        categoryGroup: CategoryGroupDTO,
        konseptGruppering: KonseptGrupperingDTO
    ){
        konseptGruppering.esco?.let { escoCategory ->
            CategoryDTO(
                code = escoCategory.uri,
                categoryType = CategoryType.ESCO,
                name = escoCategory.label
            )
        }?.let { categoryGroup.categoryList.add(it)}
    }

    private fun addStyrkToCategoriesIfExists(
        categoryGroup: CategoryGroupDTO,
        konseptGruppering: KonseptGrupperingDTO
    ){
        konseptGruppering.styrk08SSB.first()?.let { styrkCode ->
            styrkCodeConverter.lookup(styrkCode).get().let {
                CategoryDTO(
                    code = it.styrkCode,
                    categoryType = CategoryType.STYRK08,
                    name = it.styrkDescription
                )
            }
        }?.let { categoryGroup.categoryList.add(it)}
    }

    private fun String.replaceAmpersand(): String {
        return this.replace("&amp;", "&")
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

