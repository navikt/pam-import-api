package no.nav.arbeidsplassen.importapi.provider

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import java.util.UUID
import no.nav.arbeidsplassen.importapi.security.Roles
import no.nav.arbeidsplassen.importapi.security.TokenService
import org.slf4j.LoggerFactory


// TODO @Hidden
class ProviderController(
    private val providerService: ProviderService,
    private val tokenService: TokenService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProviderController::class.java)

        private fun Context.providerIdParam(): Long = pathParam("id").toLong()
        private fun Context.resetKeyParam(): Boolean = queryParam("resetKey")?.toBoolean() ?: false
        private fun Context.providerBody(): ProviderDTO = this.bodyAsClass(ProviderDTO::class.java)
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get("/internal/providers/{id}", { getProvider(it) }, Roles.ROLE_ADMIN)
        javalin.post("/internal/providers", { createProvider(it) }, Roles.ROLE_ADMIN)
        javalin.put("/internal/providers/{id}", { updateProvider(it) }, Roles.ROLE_ADMIN)
        javalin.put("/internal/providers/{id}/token", { generateNewTokenForProvider(it) }, Roles.ROLE_ADMIN)
    }

    fun getProvider(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val provider = providerService.findById(providerId)
        ctx.status(HttpStatus.OK).json(provider)
    }

    fun createProvider(ctx: Context) {
        val provider = ctx.providerBody()
        LOG.info("Creating provider ${provider.identifier}")
        val saved = providerService.save(provider)
        ctx.status(HttpStatus.CREATED).json(saved)
    }

    fun updateProvider(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val provider = ctx.providerBody()
        val updated = providerService.findById(providerId).copy(
            email = provider.email, identifier = provider.identifier, phone = provider.phone
        )
        ctx.status(HttpStatus.OK).json(providerService.save(updated))
    }

    // NOTE set resetKey to true only if you want to disable all previous key.
    fun generateNewTokenForProvider(ctx: Context) {
        val providerId = ctx.providerIdParam()
        val resetKey = ctx.resetKeyParam()
        LOG.info("Token generated for provider $providerId reset key $resetKey")
        val provider =
            if (resetKey) providerService.save(
                providerService.findById(providerId).copy(jwtid = UUID.randomUUID().toString())
            )
            else providerService.findById(providerId)
        ctx.status(HttpStatus.OK).json(tokenService.token(provider))
    }

}
