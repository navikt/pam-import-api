package no.nav.arbeidsplassen.importapi.config

import no.nav.arbeidsplassen.importapi.adadminstatus.AdminStatusController
import no.nav.arbeidsplassen.importapi.adpuls.AdPulsController
import no.nav.arbeidsplassen.importapi.adstate.AdPreviewController
import no.nav.arbeidsplassen.importapi.adstate.AdStateController
import no.nav.arbeidsplassen.importapi.adstate.AdStateInternalController
import no.nav.arbeidsplassen.importapi.nais.NaisController
import no.nav.arbeidsplassen.importapi.properties.CategoryMapsController
import no.nav.arbeidsplassen.importapi.properties.PropertiesEnumController
import no.nav.arbeidsplassen.importapi.properties.PropertyNameValueValidation
import no.nav.arbeidsplassen.importapi.provider.ProviderController
import no.nav.arbeidsplassen.importapi.transferlog.TransferController
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter

class ControllerApplicationContext(
    secretSignatureConfigProperties: SecretSignatureConfigProperties,
    controllerConfigProperties: ControllerConfigProperties,
    baseServicesApplicationContext: BaseServicesApplicationContext,
    servicesApplicationContext: ServicesApplicationContext,
    securityServicesApplicationContext: SecurityServicesApplicationContext,
    outgoingPortsApplicationContext: OutgoingPortsApplicationContext,
) {

    val naisController = NaisController(
        healthService = servicesApplicationContext.healthService,
        prometheusMeterRegistry = baseServicesApplicationContext.prometheusRegistry,
        secretSignatureConfigProperties = secretSignatureConfigProperties
    )
    val providerController by lazy {
        ProviderController(
            providerService = securityServicesApplicationContext.providerService,
            tokenService = securityServicesApplicationContext.tokenService
        )
    }
    val transferController: TransferController = TransferController(
        transferLogService = servicesApplicationContext.transferLogService,
        providerService = securityServicesApplicationContext.providerService,
        adStateService = servicesApplicationContext.adStateService,
        objectMapper = baseServicesApplicationContext.objectMapper,
        adsSize = controllerConfigProperties.transferlogBatchSize
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
    val categoryMapsController: CategoryMapsController = CategoryMapsController(
        outgoingPortsApplicationContext.ontologiGateway,
        styrkCodeConverter = StyrkCodeConverter()
    )
    val propertiesEnumController: PropertiesEnumController = PropertiesEnumController(PropertyNameValueValidation())
    val controllers = setOf(
        naisController,
        providerController,
        transferController,
        adStateController,
        adStateInternalController,
        adPreviewController,
        adminStatusController,
        adPulsController,
        categoryMapsController,
        propertiesEnumController,
    )
}
