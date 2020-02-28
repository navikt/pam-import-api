package no.nav.arbeidsplassen.importapi

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider
import no.nav.pam.yrkeskategorimapper.StyrkCodeConverter
import javax.inject.Singleton
import javax.sql.DataSource

@Singleton
class ObjectMapperBeanEventListener(): BeanCreatedEventListener<ObjectMapper> {
    override fun onCreated(event: BeanCreatedEvent<ObjectMapper>): ObjectMapper {
        val objectMapper = event.bean
        objectMapper.registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        return objectMapper
    }
}

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