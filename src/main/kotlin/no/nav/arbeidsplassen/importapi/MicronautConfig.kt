package no.nav.arbeidsplassen.importapi

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import javax.inject.Named
import javax.inject.Singleton
import javax.sql.DataSource

@Factory
class MicronautConfig(private val dataSource: DataSource) {

    @Singleton
    fun lockProvider(): LockProvider {
        return JdbcLockProvider(dataSource)
    }

    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter();
    }

}
