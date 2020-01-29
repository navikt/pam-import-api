package no.nav.arbeidsplassen.importapi

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.nav.arbeidsplassen.importapi")
                .mainClass(Application.javaClass)
                .start()
    }
}