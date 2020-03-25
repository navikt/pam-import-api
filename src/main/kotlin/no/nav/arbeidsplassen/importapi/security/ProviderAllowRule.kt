package no.nav.arbeidsplassen.importapi.security

import io.micronaut.http.HttpRequest
import io.micronaut.security.rules.AbstractSecurityRule
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.RolesFinder
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import no.nav.arbeidsplassen.importapi.provider.ProviderService
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ProviderAllowRule(private val rolesFinder: RolesFinder,
                        private val providerService: ProviderService): AbstractSecurityRule(rolesFinder) {

    private val ORDER = SecuredAnnotationRule.ORDER - 1

    companion object {
        private val LOG = LoggerFactory.getLogger(ProviderAllowRule::class.java)
    }

    override fun check(request: HttpRequest<*>, routeMatch: RouteMatch<*>?, claims: MutableMap<String, Any>?): SecurityRuleResult {
        if (routeMatch is MethodBasedRouteMatch<*, *> && !claims.isNullOrEmpty()) {
        }
        return SecurityRuleResult.UNKNOWN
    }

    override fun getOrder(): Int = ORDER

}