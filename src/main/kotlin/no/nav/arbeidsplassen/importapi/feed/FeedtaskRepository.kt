package no.nav.arbeidsplassen.importapi.feed

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
abstract class FeedtaskRepository: CrudRepository<Feedtask, String> {

    abstract fun findByName(name: String): Optional<Feedtask>
}
