package no.nav.arbeidsplassen.importapi.config

import io.javalin.Javalin

interface JavalinController {

    fun setupRoutes(javalin: Javalin)
}
