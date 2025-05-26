package no.nav.arbeidsplassen.importapi.config

import org.slf4j.LoggerFactory

interface OnServerStartup {

    companion object {
        private val LOG = LoggerFactory.getLogger(OnServerStartup::class.java)
    }

    fun onServerStartup() {
        LOG.info("Running on server startup, doing nothing")
    }
}
