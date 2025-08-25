package no.nav.arbeidsplassen.importapi.security

import io.javalin.security.RouteRole

enum class Roles : RouteRole { ROLE_ADMIN, ROLE_USER, ROLE_PROVIDER, ROLE_UNPROTECTED }
