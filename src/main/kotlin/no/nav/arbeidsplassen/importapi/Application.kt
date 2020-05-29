package no.nav.arbeidsplassen.importapi

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme

@OpenAPIDefinition(
        info = Info(
                title = "Arbeidsplassen import api",
                version = "1.0",
                description = "Import api for available jobs"
        )
)
@SecurityScheme(name = "bearer-auth", type = SecuritySchemeType.HTTP, scheme = "bearer",
        `in` = SecuritySchemeIn.HEADER, bearerFormat = "JWT")
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.nav.arbeidsplassen.importapi")
                .mainClass(Application.javaClass)
                .start()
    }
}
