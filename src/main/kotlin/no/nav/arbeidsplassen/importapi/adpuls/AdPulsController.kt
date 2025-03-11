package no.nav.arbeidsplassen.importapi.adpuls

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.annotation.Nullable
import no.nav.arbeidsplassen.importapi.repository.Pageable
import no.nav.arbeidsplassen.importapi.repository.RestOrderBy
import no.nav.arbeidsplassen.importapi.repository.RestPageable
import no.nav.arbeidsplassen.importapi.repository.RestSlice
import no.nav.arbeidsplassen.importapi.repository.RestSortable
import no.nav.arbeidsplassen.importapi.repository.Slice
import no.nav.arbeidsplassen.importapi.repository.Sortable
import no.nav.arbeidsplassen.importapi.security.ProviderAllowed
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

@ProviderAllowed(value = [Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN])
@Controller("/api/v1/stats/")
@SecurityRequirement(name = "bearer-auth")
class AdPulsController(private val adPulsService: AdPulsService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsController::class.java)
    }

    @Get("/{providerId}")
    fun getAllTodayStatsForProvider(
        @PathVariable providerId: Long,
        @QueryValue from: String,
        @Nullable @QueryValue page: Long?,
        @Nullable @QueryValue number: Long?,
        @Nullable @QueryValue size: Int?,
        @Nullable @QueryValue sort: List<String>?,
    ): RestSlice<AdPulsDTO> {
        LOG.info("Entering getAllTodayStatsForProvider")
        val fromDate = LocalDateTime.parse(from).truncatedTo(ChronoUnit.HOURS)
        LOG.info("Getting stats for provider $providerId from $fromDate")
        require(fromDate.isAfter(LocalDateTime.now().minusHours(24))) { "date is out of range, max 24h from now" }

        val pamImportPageable = mapPageable(page, number, size, sort)
        return mapSlice(adPulsService.findByProviderIdAndUpdatedAfter(providerId, fromDate, pamImportPageable))
    }

    private fun <T> mapSlice(slice: Slice<T>): RestSlice<T> {
        return RestSlice(
            content = slice.content,
            pageable = RestPageable(
                size = slice.pageable.size,
                number = slice.pageable.number,
                sort = RestSortable(
                    orderBy = listOf(
                        RestOrderBy(
                            property = slice.pageable.sort.property.name.lowercase(),
                            direction = slice.pageable.sort.direction.name,
                            ignoreCase = false,
                            ascending = (slice.pageable.sort.direction == Sortable.Direction.ASC)
                        )
                    )
                )
            )
        )
    }

    private fun mapPageable(
        pageFromUrl: Long?,
        numberFromUrl: Long?,
        sizeFromUrl: Int?,
        sortFromUrl: List<String>?
    ): Pageable {
        val lowercaseSort = sortFromUrl?.map { it.lowercase() } ?: emptyList()
        val page = pageFromUrl ?: numberFromUrl ?: 0
        val size = sizeFromUrl ?: 1000

        validatePageable(page, size, lowercaseSort)
        val sortProperty = extractSortableProperty(lowercaseSort) ?: Sortable.Property.UPDATED
        val sortDirection = extractSortableDirection(lowercaseSort) ?: Sortable.Direction.ASC
        return Pageable(
            size = size,
            number = page,
            sort = Sortable(property = sortProperty, direction = sortDirection)
        )
    }

    private fun validatePageable(page: Long, size: Int, sort: List<String>) {
        require(page >= 0) { "size can not be less than 0" }
        require(size <= 1000) { "size can not be more than 1000" }
        require(
            sort.filterNot { it == "updated" || it == "created" || it == "asc" || it == "desc" }.isEmpty()
        ) { "only legal values for sort are updated, created, asc and desc" }
        require(
            sort.filter { it == "updated" || it == "created" }.size <= 1
        ) { "We do not support ordering by more than one property" }
        require(
            sort.filter { it == "asc" || it == "desc" }.size <= 1
        ) { "We do not support ordering in both directions" }
    }

    private fun extractSortableProperty(sortFromUrl: List<String>): Sortable.Property? {
        if (sortFromUrl.contains("updated")) {
            return Sortable.Property.UPDATED
        }
        if (sortFromUrl.contains("created")) {
            return Sortable.Property.CREATED
        }
        return null
    }

    private fun extractSortableDirection(sortFromUrl: List<String>): Sortable.Direction? {
        if (sortFromUrl.contains("asc")) {
            return Sortable.Direction.ASC
        }
        if (sortFromUrl.contains("desc")) {
            return Sortable.Direction.DESC
        }
        return null
    }
}
