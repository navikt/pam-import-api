package no.nav.arbeidsplassen.importapi.security

import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Inherited
@Retention(AnnotationRetention.RUNTIME)
annotation class ProviderAllowed (vararg val value: String)