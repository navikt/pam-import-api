package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.annotation.security.PermitAll

@PermitAll
@Controller("/internal/transfers")
@Hidden
class TransferlogInternalController(private val transferLogRepository: TransferLogRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferlogInternalController::class.java)
    }

    @Get("/")
    fun getAllTransferlogs(@QueryValue updated: String, pageable: Pageable): Slice<TransferLog> {
        return transferLogRepository.findByUpdatedGreaterThanEquals(LocalDateTime.parse(updated),pageable)
    }

}
