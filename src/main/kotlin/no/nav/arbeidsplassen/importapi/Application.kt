package no.nav.arbeidsplassen.importapi

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
        info = Info(
                title = "Arbeidsplassen import api",
                version = "1.0",
                description = "Import api for stillinger"
        )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.nav.arbeidsplassen.importapi")
                .mainClass(Application.javaClass)
                .start()
    }
}
