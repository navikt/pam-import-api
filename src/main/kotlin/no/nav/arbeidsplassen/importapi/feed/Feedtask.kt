package no.nav.arbeidsplassen.importapi.feed

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Feedtask(
        @Id
        val name: String,
        val lastrun: LocalDateTime = LocalDateTime.now()
)

data class FeedTransport<T>(val last: Boolean, val totalPages: Int, val totalElements: Int, val size: Int, val number: Int,
                            val first: Boolean, val numberOfElements: Int, val content: List<T>)
