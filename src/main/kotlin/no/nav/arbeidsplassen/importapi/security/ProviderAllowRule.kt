package no.nav.arbeidsplassen.importapi.security

import io.micronaut.http.HttpRequest
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.AbstractSecurityRule
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.RolesFinder
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class ProviderAllowRule(rolesFinder: RolesFinder,
                        private val providerService: ProviderService): AbstractSecurityRule(rolesFinder) {

    private val ORDER = SecuredAnnotationRule.ORDER - 1

    companion object {
        private val LOG = LoggerFactory.getLogger(ProviderAllowRule::class.java)
    }

    override fun check(request: HttpRequest<*>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        if (routeMatch is MethodBasedRouteMatch<*, *> && !claims.isNullOrEmpty()) {
            if (routeMatch.hasAnnotation(ProviderAllowed::class.java)) {
                val values = routeMatch.getValue(ProviderAllowed::class.java, Array<String>::class.java).get().toMutableList()
                val roles = getRoles(claims)
                if (values.contains(Roles.ROLE_ADMIN) && roles.contains(Roles.ROLE_ADMIN)) {
                    LOG.debug("Admin request allow")
                    return SecurityRuleResult.ALLOWED
                }
                val providerId = routeMatch.variableValues["providerId"].toString().toLong()
                if (providerId != claims["providerId"]) {
                    LOG.debug("Rejected because provider id does not match with claims")
                    return SecurityRuleResult.REJECTED
                }
                val provider = providerService.findById(providerId)
                if (provider.jwtid != claims["jti"]) {
                    LOG.debug("Rejected because jwt id does not match with claims")
                    return SecurityRuleResult.REJECTED
                }
                return compareRoles(values, roles)
            }
        }
        return SecurityRuleResult.UNKNOWN
    }

    override fun getOrder(): Int = ORDER

}