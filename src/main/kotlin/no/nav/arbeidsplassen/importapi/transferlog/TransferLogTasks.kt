package no.nav.arbeidsplassen.importapi.transferlog

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import no.nav.arbeidsplassen.importapi.dao.*
import no.nav.arbeidsplassen.importapi.dto.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import javax.inject.Singleton
import kotlin.streams.toList

@Singleton
class TransferLogTasks(private val transferLogService: TransferLogService,
                       @Value("\${transferlog.size:50}") private val logSize: Int,
                       @Value("\${transferlog.delete.months:6}") private val deleteMonths: Long) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferLogTasks::class.java)
    }

    fun doTransferLogTask() {
        val transferlogs = transferLogService.findByStatus(TransferLogStatus.RECEIVED, Pageable.from(0,logSize))
        LOG.info("received ${transferlogs.size}")
        transferlogs.stream().forEach {
            transferLogService.mapTransferLog(it)
        }
    }

    fun deleteTransferLogTask(date: LocalDateTime = LocalDateTime.now().minusMonths(deleteMonths)) {
        LOG.info("Deleting transferlog before $date")
        transferLogService.deleteByUpdatedBefore(date)
    }

}