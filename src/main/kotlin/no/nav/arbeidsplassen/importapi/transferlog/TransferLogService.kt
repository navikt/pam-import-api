package no.nav.arbeidsplassen.importapi.transferlog

import no.nav.arbeidsplassen.importapi.exception.ImportApiError
import no.nav.arbeidsplassen.importapi.exception.ErrorType
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO

import jakarta.inject.Singleton
import no.nav.arbeidsplassen.importapi.dto.AdDTO
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.properties.PropertyNames
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter

@Singleton
class TransferLogService(private val transferLogRepository: TransferLogRepository,
                         private val styrkCodeConverter: StyrkCodeConverter,
                         private val propertyNameValueValidation: PropertyNameValueValidation) {


    fun existsByProviderIdAndMd5(providerId: Long, md5: String):
            Boolean = transferLogRepository.existsByProviderIdAndMd5(providerId, md5)

    fun save(dto: TransferLogDTO): TransferLogDTO {
        return transferLogRepository.save(dto.toEntity()).toDTO()
    }

    fun findByVersionIdAndProviderId(versionId: Long, providerId: Long): TransferLogDTO {
       return transferLogRepository.findByIdAndProviderId(versionId, providerId)
               .orElseThrow{ ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND) }
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
        return TransferLogDTO(versionId = id!!, message = message, status = status,
                md5 = md5, created = created, updated = updated, payload = payload, items = items, providerId = providerId)
    }

    fun resend(versionId: Long):TransferLogDTO {
        val received = transferLogRepository.findById(versionId).orElseThrow {
            ImportApiError("Transfer $versionId not found", ErrorType.NOT_FOUND)
        }.copy(status = TransferLogStatus.RECEIVED)
        return transferLogRepository.save(received).toDTO()
    }

    fun updateExpiresIfNullAndStarttimeSnarest(ad: AdDTO) : AdDTO {
        if ("SNAREST" == ad.properties[PropertyNames.applicationdue]?.uppercase()
            && ad.expires == null) {
            val newExpiryDate = ad.published?.plusDays(10)
            return ad.copy(expires = newExpiryDate)
        } else {
            return ad
        }
    }

    /** Vi ønsker å få inn annonsen selv om kategorien er feil, da vi uansett gjør en automatisk klassifisering mot Janzz */
    fun removeInvalidCategories(ad: AdDTO): AdDTO {
        return ad.copy(categoryList = ad.categoryList.stream()
            .filter { cat -> !styrkCodeConverter.lookup(cat.code).isEmpty }
            .toList()
        )
    }

    fun validate(ad: AdDTO) {
        if (ad.categoryList.count()>3) {
            throw ImportApiError("category list is over 3, we only allow max 3 categories per ad", ErrorType.INVALID_VALUE)
        }
        if (!locationMustHavePostalCodeOrCountyMunicipal(ad)) {
            throw ImportApiError("Location does not have postal code, or does not have county/municipality", ErrorType.INVALID_VALUE)
        }
        ad.categoryList.stream().forEach { cat ->
            val optCat = styrkCodeConverter.lookup(cat.code)
            if (optCat.isEmpty) throw ImportApiError("category ${cat.code} does not exist", ErrorType.INVALID_VALUE)
        }
        propertyNameValueValidation.checkOnlyValidValues(ad.properties)
    }

    private fun locationMustHavePostalCodeOrCountyMunicipal(ad:AdDTO) = (ad.locationList.isNotEmpty()
            && (!ad.locationList[0].postalCode.isNullOrEmpty() ||
            (!ad.locationList[0].county.isNullOrEmpty() && !ad.locationList[0].municipal.isNullOrEmpty())))

}
