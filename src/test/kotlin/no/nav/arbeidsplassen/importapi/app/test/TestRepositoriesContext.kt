package no.nav.arbeidsplassen.importapi.app.test

import no.nav.arbeidsplassen.importapi.config.BaseServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.DatabaseApplicationContext
import no.nav.arbeidsplassen.importapi.config.SecurityServicesApplicationContext
import no.nav.arbeidsplassen.importapi.config.TestDatabaseConfigProperties
import no.nav.arbeidsplassen.importapi.config.TestSecretSignatureConfigProperties

/*
 * Application context som kan brukes i tester
 */
class TestRepositoriesContext(host: String, port: String) {
    val secretSignatureConfigProperties = TestSecretSignatureConfigProperties()
    val databaseConfigurationProperties = TestDatabaseConfigProperties(host, port)
    val baseServicesApplicationContext = BaseServicesApplicationContext()
    val databaseApplicationContext = DatabaseApplicationContext(
        databaseConfigProperties = databaseConfigurationProperties,
        baseServicesApplicationContext = baseServicesApplicationContext
    )
    val securityServicesApplicationContext = SecurityServicesApplicationContext(
        secretSignatureConfigProperties = secretSignatureConfigProperties,
        databaseApplicationContext = databaseApplicationContext
    )
}
