package no.nav.arbeidsplassen.importapi.transferlog

import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryDTO
import no.nav.arbeidsplassen.importapi.dto.CategoryType
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException


@Singleton
class TransferLogService(
    private val transferLogRepository: TransferLogRepository,
    private val propertyNameValueValidation: PropertyNameValueValidation,
    private val ontologiGateway: LokalOntologiGateway
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogService::class.java)
    }


    fun existsByProviderIdAndMd5(providerId: Long, md5: String):
            Boolean = transferLogRepository.existsByProviderIdAndMd5(providerId, md5)

    fun save(dto: TransferLogDTO): TransferLogDTO {
        return transferLogRepository.save(dto.toEntity()).toDTO()
    }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): TransferLogDTO {
        return transferLogRepository.findByIdAndProviderId(versionId, providerId)
            .orElseThrow { ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND) }
            .toDTO()
    }

    fun findByVersionId(versionId: Long): TransferLogDTO {
        return transferLogRepository.findById(versionId).orElseThrow {
            ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)
        }.toDTO()
    }

    private fun TransferLogDTO.toEntity(): TransferLog {
        return TransferLog(providerId = providerId, md5 = md5, payload = payload!!, items = items, message = message)
    }

    private fun TransferLog.toDTO(): TransferLogDTO {
        return TransferLogDTO(
            versionId = id!!, message = message, status = status,
            md5 = md5, created = created, updated = updated, payload = payload, items = items, providerId = providerId
        )
    }

    fun resend(versionId: Long): TransferLogDTO {
        val received = transferLogRepository.findById(versionId).orElseThrow {
            ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)
        }.copy(status = TransferLogStatus.RECEIVED)
        return transferLogRepository.save(received).toDTO()
    }

    fun handleExpiryAndStarttimeCombinations(ad: AdDTO): AdDTO {
        if ("SNAREST" == ad.properties[PropertyNames.applicationdue]?.uppercase()
            && ad.expires == null
        ) {
            val newExpiryDate = ad.published?.plusDays(10)
            return ad.copy(expires = newExpiryDate)
        }
        parseApplicationDueDate(ad.properties[PropertyNames.applicationdue])?.atStartOfDay()?.run {
            if (ad.expires != null && this.isBefore(ad.expires)) {
                return ad.copy(expires = this)
            }
        }
        return ad
    }


    private fun parseApplicationDueDate(applicationDue: String?): LocalDate? {
        val dateTimeFormatterBuilder = DateTimeFormatterBuilder()
            .append(
                DateTimeFormatter.ofPattern(
                    "[yyyy-MM-dd'T'HH:mm:ss]"
                            + "[yyyy-MM-dd'T'HH:mm]"
                            + "[dd.MM.yyyy]"
                )
            )

        val dateTimeFormatter = dateTimeFormatterBuilder.toFormatter()
        return try {
            LocalDate.parse(applicationDue, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** Vi ønsker å få inn annonsen selv om kategorien er feil / ugyldig, da vi uansett gjør en automatisk klassifisering mot Janzz */
    fun handleInvalidCategories(ad: AdDTO, providerId: Long, reference: String): AdDTO {
        val invalidCategories: List<CategoryDTO> = findInvalidCategories(ad, providerId, reference)
        return ad.copy(properties = addInvalidCategoriesToProperties(invalidCategories, ad.properties.toMutableMap()),
            categoryList = ad.categoryList.filter { cat -> !invalidCategories.contains(cat) })
    }

    private fun findInvalidCategories(ad: AdDTO, providerId: Long, reference: String): List<CategoryDTO> {
        return ad.categoryList
            .filter { cat ->
                if (cat.categoryType != CategoryType.JANZZ) {
                    true
                } else if (cat.categoryType == CategoryType.JANZZ && (cat.name.isNullOrEmpty() || cat.code.isNullOrEmpty())) {
                    true
                } else {
                    cat.name?.let { janzztittel ->
                        try {
                            val typeaheads = ontologiGateway.hentTypeaheadStilling(janzztittel)
                            typeaheads
                                .any { typeahead ->
                                    LOG.info(
                                        "Mottatt typeahead {} for {} og kode {} for kode {}" + typeahead.name,
                                        janzztittel,
                                        typeahead.code.toString(),
                                        cat.code
                                    )
                                    (janzztittel.equals(
                                        typeahead.name,
                                        ignoreCase = true
                                    )) && (typeahead.code.toString() == cat.code)
                                }
                        } catch (e: Exception) {
                            LOG.error("Feiler i typeaheadkall mot ontologien og vil fjerne satt JANZZ-kategori", e)
                            false
                        }
                    } == false
                }
            }
            .toList()
    }

    private fun addInvalidCategoriesToProperties(
        invalidCategories: List<CategoryDTO>,
        properties: MutableMap<PropertyNames, String>
    ): MutableMap<PropertyNames, String> {
        var invalidCategoriesFormatted = invalidCategories.stream()
            .map { cat -> cat.name }.toList().joinToString(separator = ";")

        properties[PropertyNames.keywords] =
            if (!properties[PropertyNames.keywords].isNullOrEmpty() && !invalidCategoriesFormatted.isNullOrEmpty())
                invalidCategoriesFormatted.plus(";").plus(properties[PropertyNames.keywords])
            else
                properties[PropertyNames.keywords] ?: invalidCategoriesFormatted

        return properties
    }


    fun validate(ad: AdDTO) {
        if (ad.categoryList.count() > 3) {
            throw ImportApiError(
                "category list is over 3, we only allow max 3 categories per ad",
                ErrorType.INVALID_VALUE
            )
        }
        if (!locationMustHavePostalCodeOrCountyMunicipal(ad) && !isCountryAbroad(ad)) {
            LOG.info("Avviser stilling da den ikke har postnummer eller fylke/kommune satt (og ikke er utenlandsstilling). Location: ${ad.locationList.firstOrNull()}");
            throw ImportApiError(
                "Location does not have postal code, or does not have county/municipality",
                ErrorType.INVALID_VALUE
            )
        }
        propertyNameValueValidation.checkOnlyValidValues(ad.properties)
    }

    private fun locationMustHavePostalCodeOrCountyMunicipal(ad: AdDTO) = (ad.locationList.isNotEmpty()
            && (!ad.locationList[0].postalCode.isNullOrEmpty() ||
            (!ad.locationList[0].county.isNullOrEmpty() && !ad.locationList[0].municipal.isNullOrEmpty())))

    private fun isCountryAbroad(ad: AdDTO) = ad.locationList.isNotEmpty() && ad.locationList[0].hasOnlyCountrySet()
            && !listOf("NORGE", "NOREG", "NORWAY").contains(ad.locationList[0].country?.uppercase())

}
