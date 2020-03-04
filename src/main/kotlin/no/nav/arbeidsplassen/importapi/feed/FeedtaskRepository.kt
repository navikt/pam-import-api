package no.nav.arbeidsplassen.importapi.feed

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository

@JdbcRepository
abstract class FeedtaskRepository: CrudRepository<Feedtask, String> {

    abstract fun findByName(name: String): Feedtask
}