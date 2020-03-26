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
                val optionalValue = routeMatch.getValue(ProviderAllowed::class.java, Array<String>::class.java)
                if (optionalValue.isPresent) {
                    val values =  optionalValue.get().toMutableList()
                    return compareRoles(values, getRoles(claims))
                }
            }
        }
        return SecurityRuleResult.UNKNOWN
    }

    override fun getOrder(): Int = ORDER

}