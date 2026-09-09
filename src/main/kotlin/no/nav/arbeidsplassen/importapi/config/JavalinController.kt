package no.nav.arbeidsplassen.importapi.config

import io.javalin.config.JavalinConfig

interface JavalinController {

    fun setupRoutes(javalin: JavalinConfig)
}
