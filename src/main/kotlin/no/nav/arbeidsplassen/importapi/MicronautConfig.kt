package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter

@Factory
class MicronautConfig {
    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter()
    }
}