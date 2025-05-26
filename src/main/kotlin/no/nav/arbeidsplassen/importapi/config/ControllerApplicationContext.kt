package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusController
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsController
import no.nav.arbeidsplassen.importapi.adstate.AdPreviewController
import no.nav.arbeidsplassen.importapi.adstate.AdStateController
import no.nav.arbeidsplassen.importapi.adstate.AdStateInternalController
import no.nav.arbeidsplassen.importapi.nais.NaisController
import no.nav.arbeidsplassen.importapi.provider.ProviderController
import no.nav.arbeidsplassen.importapi.transferlog.TransferController


class ControllerConfigProperties(
    val adPreviewUrl: String,
    val adsSize: Int = 100,
) {
    companion object {
        fun ControllerConfigProperties(env: Map<String, String>): ControllerConfigProperties =
            ControllerConfigProperties(
                adPreviewUrl = env.variable("ad.preview.url"),
                adsSize = env.nullableVariable("transferlog.batch-size")?.toInt() ?: 100,
            )
    }
}

class ControllerApplicationContext(
    secretSignatureConfigProperties: SecretSignatureConfigProperties,
    controllerConfigProperties: ControllerConfigProperties,
    baseServicesApplicationContext: BaseServicesApplicationContext,
    servicesApplicationContext: ServicesApplicationContext,
) {

    val naisController = NaisController(
        healthService = servicesApplicationContext.healthService,
        prometheusMeterRegistry = baseServicesApplicationContext.prometheusRegistry,
        secretSignatureConfigProperties = secretSignatureConfigProperties
    )
    val providerController by lazy {
        ProviderController(
            providerService = servicesApplicationContext.providerService,
            tokenService = servicesApplicationContext.tokenService
        )
    }
    val transferController: TransferController = TransferController(
        transferLogService = servicesApplicationContext.transferLogService,
        providerService = servicesApplicationContext.providerService,
        adStateService = servicesApplicationContext.adStateService,
        objectMapper = baseServicesApplicationContext.objectMapper,
        adsSize = controllerConfigProperties.adsSize
    )
    val adStateController: AdStateController = AdStateController(servicesApplicationContext.adStateService)
    val adStateInternalController: AdStateInternalController =
        AdStateInternalController(servicesApplicationContext.adStateService)
    val adPreviewController: AdPreviewController = AdPreviewController(
        adStateService = servicesApplicationContext.adStateService,
        previewUrl = controllerConfigProperties.adPreviewUrl
    )
    val adminStatusController: AdminStatusController =
        AdminStatusController(servicesApplicationContext.adminStatusService)
    val adPulsController: AdPulsController = AdPulsController(servicesApplicationContext.adPulsService)
}
