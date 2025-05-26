package no.nav.arbeidsplassen.importapi.adoutbox

import no.nav.arbeidsplassen.importapi.nais.HealthService
import org.apache.kafka.clients.producer.RecordMetadata

class AdOutboxKafkaProducer(
    private val synchronousKafkaSendAndGet: SynchronousKafkaSendAndGet,
    private val topic: String,
    private val healthService: HealthService,
) {
    fun sendAndGet(uuid: String, payload: ByteArray, meldingstype: Meldingstype): RecordMetadata =
        synchronousKafkaSendAndGet.sendAndGet(topic, uuid, payload, meldingstype)

    fun unhealthy() = healthService.addUnhealthyVote()
}
