package no.nav.arbeidsplassen.importapi.adstate

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import no.nav.arbeidsplassen.importapi.dto.AdStatePublicDTO
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class AdStateController(private val adStateService: AdStateService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdStateController::class.java)
        private fun Context.providerIdParam(): Long = pathParam("providerId").toLong()
        private fun Context.referenceParam(): String = pathParam("reference")
        private fun Context.uuidParam(): String = pathParam("uuid")
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/api/v1/adstates/{providerId}/{reference}",
            { getAdStateByProviderReference(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )

        javalin.get(
            "/api/v1/adstates/{providerId}/uuid/{uuid}",
            { getAdStateByUuid(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
    }

    fun getAdStateByProviderReference(ctx: Context) {
        val providerId: Long = ctx.providerIdParam()
        val reference: String = ctx.referenceParam()
        val adState: AdStatePublicDTO = adStateService.getAdStatesByProviderReference(providerId, reference)
        ctx.status(HttpStatus.OK).json(adState)
    }


    fun getAdStateByUuid(ctx: Context) {
        val providerId: Long = ctx.providerIdParam()
        val uuid: String = ctx.uuidParam()
        val adState = adStateService.getAdStateByUuidAndProviderId(uuid, providerId)
        ctx.status(HttpStatus.OK).json(adState)
    }


    /*
    // HPH: Kommenterer ut denne da jeg ikke finner at den er i bruk i accessloggene
    @Get("/{providerId}/versions/{versionId}")
    fun getAdStatesByProvider(@PathVariable providerId: Long, @PathVariable versionId: Long, pageable: Pageable): Slice<AdStatePublicDTO> {
        return adStateService.getAdStatesByVersionIdAndProviderId(versionId, providerId, pageable)
    }
    */

}
