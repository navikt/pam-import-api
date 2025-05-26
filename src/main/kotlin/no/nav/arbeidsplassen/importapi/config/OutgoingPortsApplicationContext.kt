package no.nav.arbeidsplassen.importapi.config

import java.net.http.HttpClient
import no.nav.arbeidsplassen.importapi.adoutbox.KafkaAdOutboxSendAndGet
import no.nav.arbeidsplassen.importapi.adoutbox.SynchronousKafkaSendAndGet
import no.nav.arbeidsplassen.importapi.kafka.KafkaConfig
import no.nav.arbeidsplassen.importapi.leaderelection.LeaderElection
import no.nav.arbeidsplassen.importapi.leaderelection.NaisLeaderElection
import no.nav.arbeidsplassen.importapi.ontologi.LokalOntologiGateway
import no.nav.arbeidsplassen.importapi.ontologi.OntologiGateway

interface OutgoingPortsApplicationContext {
    val synchronousKafkaSendAndGet: SynchronousKafkaSendAndGet
    val leaderElection: LeaderElection
    val ontologiGateway: OntologiGateway
}

class OutgoingPortsConfigurationProperties(
    val ontologiBaseUrl: String,
    val electorPath: String,
) {
    companion object {
        fun OutgoingPortsConfigurationProperties(env: Map<String, String>): OutgoingPortsConfigurationProperties =
            OutgoingPortsConfigurationProperties(
                ontologiBaseUrl = env.variable("pam.ontologi.typeahead.url"),
                electorPath = env.nullableVariable("ELECTOR_PATH") ?: "NOLEADERELECTION",
            )
    }
}

open class DefaultOutgoingPortsApplicationContext(
    baseServicesApplicationContext: BaseServicesApplicationContext,
    outgoingPortsConfigurationProperties: OutgoingPortsConfigurationProperties,
    kafkaConfig: KafkaConfig,
) : OutgoingPortsApplicationContext {

    override val synchronousKafkaSendAndGet: SynchronousKafkaSendAndGet = KafkaAdOutboxSendAndGet(
        kafkaProducer = kafkaConfig.kafkaProducer()
    )

    val httpClient: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(HttpClient.Version.HTTP_1_1)
        .build()

    override val leaderElection: LeaderElection = NaisLeaderElection(
        httpClient = httpClient,
        electorPath = outgoingPortsConfigurationProperties.electorPath,
        objectMapper = baseServicesApplicationContext.objectMapper,
    )

    override val ontologiGateway: OntologiGateway = LokalOntologiGateway(
        baseurl = outgoingPortsConfigurationProperties.ontologiBaseUrl
    )
}
