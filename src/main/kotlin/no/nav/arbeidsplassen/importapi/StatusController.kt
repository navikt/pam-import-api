package no.nav.arbeidsplassen.importapi

import io.micronaut.configuration.kafka.ConsumerRegistry
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.token.jwt.signature.secret.SecretSignatureConfiguration
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.exception.KafkaStateRegistry
import org.slf4j.LoggerFactory
import jakarta.annotation.security.PermitAll

@PermitAll
@Controller("/internal")
@Hidden
class StatusController(private val secretSignatureConfiguration: SecretSignatureConfiguration,
                       private val kafkaStateRegistry: KafkaStateRegistry,
                       private val consumerRegistry: ConsumerRegistry) {

    companion object {
        private val LOG = LoggerFactory.getLogger(StatusController::class.java)
    }

    @Get("/isReady", produces = [MediaType.TEXT_PLAIN])
    fun isReady(): HttpResponse<String> {
        if ("Thisisaverylongsecretandcanonlybeusedintest" == secretSignatureConfiguration.secret)
            return HttpResponse.serverError("Secret not set")
        return HttpResponse.ok("OK")
    }

    @Get("/isAlive",produces = [MediaType.TEXT_PLAIN])
    fun isAlive(): HttpResponse<String> {
        if (kafkaStateRegistry.hasError()) {
            LOG.error("A Kafka consumer is set to Error, setting all consumers to pause")
            consumerRegistry.consumerIds
                .forEach {
                    LOG.error("Pausing consumer $it")
                    consumerRegistry.pause(it)
                }
            return HttpResponse.serverError("Kafka consumer has error and stopped")
        }
        return HttpResponse.ok("OK")
    }


}
