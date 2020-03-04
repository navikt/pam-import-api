package no.nav.arbeidsplassen.importapi.adadminstatus

import no.nav.arbeidsplassen.importapi.feed.FeedConnector
import no.nav.arbeidsplassen.importapi.feed.FeedtaskRepository
import javax.inject.Singleton

@Singleton
class AdminStatusSync(private val feedConnector: FeedConnector,
                      private val feedtaskRepository: FeedtaskRepository) {


}