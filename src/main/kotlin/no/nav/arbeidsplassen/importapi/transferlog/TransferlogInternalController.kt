package no.nav.arbeidsplassen.importapi.transferlog

import io.micronaut.data.model.Pageable
import io.micronaut.data.model.Slice
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Hidden
import no.nav.arbeidsplassen.importapi.dto.TransferLogDTO
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.annotation.security.PermitAll

@ProviderAllowed(value = [Roles.ROLE_ADMIN])
@Controller("/internal/transfers")
@Hidden
class TransferlogInternalController(private val transferLogService: TransferLogService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferlogInternalController::class.java)
    }

    @Get("/{versionId}")
    fun getByTransferId(@PathVariable versionId: Long):TransferLogDTO {
        return transferLogService.findByVersionId(versionId)
    }

    @Put("/{versionId}/resend")
    fun resendTransfer(@PathVariable versionId: Long): TransferLogDTO {
        LOG.info("resend $versionId by admin")
        return transferLogService.resend(versionId)
    }

}
