package no.nav.arbeidsplassen.importapi.adstate

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.Topic
import io.reactivex.Flowable
import no.nav.arbeidsplassen.importapi.Open
import org.apache.kafka.clients.producer.RecordMetadata


@Open
@KafkaClient
interface AdstateKafkaSender {

    @KafkaClient(batch = true)
    @Topic("StillingImportApiAdState")
    fun send(adstates: Iterable<AdState>): Flowable<RecordMetadata>

}

