package no.nav.arbeidsplassen.importapi.transferlog

import org.apache.commons.text.StringEscapeUtils
import org.owasp.html.Sanitizers


fun sanitize(html: String): String {
    val policy = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.TABLES)
    return policy.sanitize(StringEscapeUtils.unescapeHtml4(html))
}

