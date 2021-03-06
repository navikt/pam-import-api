package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import javax.inject.Singleton

@Factory
class MicronautConfig {


    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter();
    }

}
