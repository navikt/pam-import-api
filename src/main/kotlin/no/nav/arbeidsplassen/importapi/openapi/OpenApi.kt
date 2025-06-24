import io.javalin.openapi.BearerAuth
import io.javalin.openapi.OpenApiInfo
import io.javalin.openapi.plugin.DefinitionConfiguration
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import no.nav.arbeidsplassen.importapi.security.Roles


fun getOpenApiPlugin() = OpenApiPlugin { openApiConfig ->
    openApiConfig
        .withDocumentationPath("/rest/internal/openapi.json")
        .withRoles(Roles.ROLE_UNPROTECTED)
        .withDefinitionConfiguration { _: String, definition: DefinitionConfiguration ->
            definition.withInfo { openApiInfo: OpenApiInfo ->
                openApiInfo.title = "Arbeidsplassen CV API"
                openApiInfo.description = "API for henting av brukers CV data"
                openApiInfo.version = "v2.0"
            }.withServer { openApiServer ->
                openApiServer.url = "https://pam-cv-api-gcp.intern.dev.nav.no"
                openApiServer.description = "dev-gcp"
            }.withServer { openApiServer ->
                openApiServer.url = "https://pam-cv-api-gcp.intern.nav.no"
                openApiServer.description = "prod-gcp"
            }.withSecurity(SecurityComponentConfiguration().apply {
                withSecurityScheme("BearerAuth", BearerAuth())
            })
        }
}
