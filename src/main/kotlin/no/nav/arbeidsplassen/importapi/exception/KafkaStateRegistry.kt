package no.nav.arbeidsplassen.importapi.exception

import jakarta.inject.Singleton

@Singleton
class KafkaStateRegistry {

    private val stateRegistry = hashMapOf<String, KafkaState>()

    fun setConsumerToError(consumer:String) {
        stateRegistry[consumer] = KafkaState.ERROR
    }

    fun setConsumerToPaused(consumer: String) {
        stateRegistry[consumer] = KafkaState.PAUSED
    }

    fun setConsumerToRunning(consumer: String) {
        stateRegistry[consumer] = KafkaState.RUNNING
    }

    fun setProducerToError(producer: String) {
        stateRegistry[producer] = KafkaState.ERROR
    }

    fun hasError(): Boolean {
       return (stateRegistry.isNotEmpty() && stateRegistry.values.contains(KafkaState.ERROR))
    }

}

enum class KafkaState {
    RUNNING, PAUSED, ERROR
}


