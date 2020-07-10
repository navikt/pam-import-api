package no.nav.arbeidsplassen.importapi

import com.zaxxer.hikari.HikariDataSource
import io.micronaut.context.annotation.Factory
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import javax.inject.Named
import javax.inject.Singleton

@Factory
class MicronautConfig(@Named("\${shedlock.datasource}") private val dataSource: HikariDataSource) {

    @Singleton
    fun lockProvider(): LockProvider {
        return JdbcLockProvider(dataSource)
    }

    @Singleton
    fun styrkCodeConverter(): StyrkCodeConverter {
        return StyrkCodeConverter();
    }

}
