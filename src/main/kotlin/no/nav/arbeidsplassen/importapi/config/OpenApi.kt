import io.javalin.openapi.BearerAuth
import io.javalin.openapi.OpenApiInfo
import io.javalin.openapi.plugin.DefinitionConfiguration
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.SecurityComponentConfiguration
import no.nav.arbeidsplassen.importapi.security.Roles

object OpenApiConfig {
    fun getOpenApiPlugin() = OpenApiPlugin { openApiConfig ->
        openApiConfig
            .withDocumentationPath("/openapi/arbeidsplassen-1.0-openapi.json")
            .withRoles(Roles.ROLE_UNPROTECTED)
            .withDefinitionConfiguration { _: String, definition: DefinitionConfiguration ->
                definition
                    .withInfo { openApiInfo: OpenApiInfo ->
                        openApiInfo.title = "Arbeidsplassen import api"
                        openApiInfo.description = "Import api for available jobs"
                        openApiInfo.version = "1.0"
                    }
                    .withSecurity(SecurityComponentConfiguration().apply {
                        withSecurityScheme("BearerAuth", BearerAuth())
                    })
                /*
            .withServer { openApiServer ->
                openApiServer.url = "https://arbeidsplassen-api.ekstern.dev.nav.no/stillingsimport/"
                openApiServer.description = "dev-gcp"
            }.withServer { openApiServer ->
                openApiServer.url = "https://arbeidsplassen-api.ekstern.nav.no/stillingsimport/"
                openApiServer.description = "prod-gcp"
            }
            */

            }
    }
}
