package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDateTime
import no.nav.arbeidsplassen.importapi.adoutbox.AdOutboxService
import no.nav.arbeidsplassen.importapi.adstate.AdState
import no.nav.arbeidsplassen.importapi.adstate.AdStateRepository
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryType
import no.nav.arbeidsplassen.importapi.ontologi.KonseptGrupperingDTO
import no.nav.arbeidsplassen.importapi.ontologi.OntologiGateway
import no.nav.arbeidsplassen.importapi.properties.PropertyType
import no.nav.arbeidsplassen.importapi.repository.TxTemplate
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import org.slf4j.LoggerFactory

class TransferLogTasks(
    private val transferLogRepository: TransferLogRepository,
    private val adStateRepository: AdStateRepository,
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val styrkCodeConverter: StyrkCodeConverter,
    private val lokalOntologiGateway: OntologiGateway,
    private val adOutboxService: AdOutboxService,
    private val txTemplate: TxTemplate,
    private val logSize: Int, // TODO @Value("\${transferlog.tasks-size:50}")
    private val deleteMonths: Long // TODO  @Value("\${transferlog.delete.months:6}")
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogTasks::class.java)
    }

    fun processTransferLogTask(): Int {
        val transferlogs = transferLogRepository.findByStatus(TransferLogStatus.RECEIVED)
        transferlogs.forEach {
            mapTransferLog(it)
        }
        return transferlogs.count()
    }

    fun deleteTransferLogTask(date: LocalDateTime = LocalDateTime.now().minusMonths(deleteMonths)) {
        LOG.info("Deleting transferlog before $date")
        transferLogRepository.deleteByUpdatedBefore(date)
    }

    fun mapTransferLog(transferLog: TransferLog) {
        try {
            txTemplate.doInTransaction {
                val adList: List<AdState> = mapTransferLogs(transferLog)
                LOG.info("mapping transfer ${transferLog.id} for provider ${transferLog.providerId} with updated ${transferLog.updated} found ${adList.size} ads ")
                val savedList = adStateRepository.saveAll(adList)
                transferLogRepository.save(transferLog.copy(status = TransferLogStatus.DONE))
                meterRegistry.counter("ads_received", "provider", transferLog.providerId.toString())
                    .increment(adList.size.toDouble())
                adOutboxService.lagreFlereTilOutbox(savedList)
            }
        } catch (e: Exception) {
            LOG.error("Got exception while handling transfer log ${transferLog.id}", e)
            transferLogRepository.save(transferLog.copy(status = TransferLogStatus.ERROR))
        }
    }

    private fun mapTransferLogs(transferLog: TransferLog): List<AdState> {
        val ads = objectMapper.readValue(transferLog.payload, object : TypeReference<List<AdDTO>>() {})
        return ads.stream().map { mapAdToAdState(it, transferLog) }.toList()

    }

    private fun mapAdToAdState(ad: AdDTO, transferLog: TransferLog): AdState {
        LOG.info("Mapping ad {} for providerId {} transferlog {}", ad.reference, transferLog.providerId, transferLog.id)
        val inDb: AdState? = adStateRepository.findByProviderIdAndReference(transferLog.providerId, ad.reference)
        return inDb?.copy(versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(sanitizeAd(ad)))
            ?: AdState(
                versionId = transferLog.id!!, jsonPayload = objectMapper.writeValueAsString(sanitizeAd(ad)),
                providerId = transferLog.providerId, reference = ad.reference
            )
    }

    fun sanitizeAd(ad: AdDTO): AdDTO {
        val props = ad.properties.map { (key, value) ->
            when (key.type) {
                PropertyType.HTML -> key to sanitize(value)
                else -> key to value
            }
        }.toMutableList()

        val categoryList = ad.categoryList.toMutableList()

        ad.categoryList.forEach { janzzCategory ->
            try {
                janzzCategory.janzzParentId = janzzCategory.code
                val konseptgruppering: KonseptGrupperingDTO? =
                    lokalOntologiGateway.hentStyrkOgEscoKonsepterBasertPaJanzz(janzzCategory.code.toLong())
                if (konseptgruppering != null) {
                    addEscoToCategoriesIfExists(categoryList, konseptgruppering)
                    addStyrkToCategoriesIfExists(categoryList, konseptgruppering)
                }
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
            categoryList = categoryList,
            properties = props.toMap(),
        )
        LOG.info(returnAd.categoryList.toString())
        return returnAd
    }

    private fun addEscoToCategoriesIfExists(
        categoryList: MutableList<CategoryDTO>,
        konseptGruppering: KonseptGrupperingDTO
    ) {
        konseptGruppering.esco?.let { escoCategory ->
            CategoryDTO(
                code = escoCategory.uri,
                categoryType = CategoryType.ESCO,
                name = escoCategory.label,
                janzzParentId = konseptGruppering.konseptId.toString()
            )
        }?.let { categoryList.add(it) }
    }

    private fun addStyrkToCategoriesIfExists(
        categoryList: MutableList<CategoryDTO>,
        konseptGruppering: KonseptGrupperingDTO
    ) {
        konseptGruppering.styrk08SSB.firstOrNull()?.let { styrkCode ->
            styrkCodeConverter.lookup(styrkCode).get().let {
                CategoryDTO(
                    code = it.styrkCode,
                    categoryType = CategoryType.STYRK08,
                    name = it.styrkDescription,
                    janzzParentId = konseptGruppering.konseptId.toString()
                )
            }
        }?.let { categoryList.add(it) }
    }

    private fun String.replaceAmpersand(): String {
        return this.replace("&amp;", "&")
    }
}
