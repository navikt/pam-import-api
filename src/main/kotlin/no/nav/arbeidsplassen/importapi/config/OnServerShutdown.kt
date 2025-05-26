package no.nav.arbeidsplassen.importapi.config

import org.slf4j.LoggerFactory

interface OnServerShutdown {

    companion object {
        private val LOG = LoggerFactory.getLogger(OnServerShutdown::class.java)
    }

    fun onServerShutdown() {
        LOG.info("Running on server shutdown, doing nothing")
    }
}
