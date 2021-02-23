package no.nav.arbeidsplassen.importapi.feed

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@MicronautTest
class FeedtaskTest(private val feedtaskRepository: FeedtaskRepository) {

    @Test
    fun readwriteFeedTaskTest() {
        feedtaskRepository.save(Feedtask(name = "runSomeMethodName"))
        val feedtask = feedtaskRepository.findByName("runSomeMethodName").get()
        assertEquals("runSomeMethodName", feedtask.name)
        assertNotNull(feedtask.lastrun)
        assertNotNull(feedtaskRepository.update(Feedtask(name="runSomeMethodName")))
    }

}
