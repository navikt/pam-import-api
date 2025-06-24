package no.nav.arbeidsplassen.importapi.adpuls

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import no.nav.arbeidsplassen.importapi.repository.Pageable
import no.nav.arbeidsplassen.importapi.repository.RestOrderBy
import no.nav.arbeidsplassen.importapi.repository.RestPageable
import no.nav.arbeidsplassen.importapi.repository.RestSlice
import no.nav.arbeidsplassen.importapi.repository.RestSliceAdPulsDTO
import no.nav.arbeidsplassen.importapi.repository.RestSortable
import no.nav.arbeidsplassen.importapi.repository.Slice
import no.nav.arbeidsplassen.importapi.repository.Sortable
import no.nav.arbeidsplassen.importapi.security.Roles
import org.slf4j.LoggerFactory

class AdPulsController(private val adPulsService: AdPulsService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AdPulsController::class.java)
        private fun Context.providerIdParam(): Long = pathParam("providerId").toLong()
        private fun Context.fromParam(): String =
            queryParam("from") ?: throw IllegalArgumentException("from is required")

        private fun Context.pageParam(): Long? = queryParam("page")?.toLong()
        private fun Context.numberParam(): Long? = queryParam("number")?.toLong()
        private fun Context.sizeParam(): Int? = queryParam("size")?.toInt()
        private fun Context.sortParams(): List<String>? = queryParams("sort").flatMap { it.split(",") }
    }

    fun setupRoutes(javalin: Javalin) {
        javalin.get(
            "/api/v1/stats/{providerId}",
            { getAllTodayStatsForProvider(it) },
            Roles.ROLE_PROVIDER, Roles.ROLE_ADMIN
        )
    }

    @OpenApi(
        path = "/stillingsimport/api/v1/stats/{providerId}",
        methods = [HttpMethod.GET],
        pathParams = [OpenApiParam(
            name = "providerId",
            type = Long::class,
            required = true,
            description = "providerId"
        )],
        queryParams = [
            OpenApiParam(name = "from", type = String::class, required = false, description = "from"),
            OpenApiParam(name = "page", type = Long::class, required = false, description = "page"),
            OpenApiParam(name = "number", type = Long::class, required = false, description = "number"),
            OpenApiParam(name = "size", type = Int::class, required = false, description = "size"),
            OpenApiParam(
                name = "sort",
                type = Array<String>::class,
                required = false,
                description = "sort",
                example = "created, updated, asc, desc"
            ),
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "getAllTodayStatsForProvider 200 response",
                content = [OpenApiContent(
                    from = RestSliceAdPulsDTO::class,
                )],
            ),
        ]
    )
    fun getAllTodayStatsForProvider(ctx: Context) {
        try {
            LOG.debug("Entering getAllTodayStatsForProvider")
            val providerId = ctx.providerIdParam()
            val from = ctx.fromParam()
            val page = ctx.pageParam()
            val number = ctx.numberParam()
            val size = ctx.sizeParam()
            val sort = ctx.sortParams()
            LOG.info("Retrieved params in getAllTodayStatsForProvider")
            val fromDate = LocalDateTime.parse(from).truncatedTo(ChronoUnit.HOURS)
            LOG.info("Getting stats for provider $providerId from $fromDate")
            require(fromDate.isAfter(LocalDateTime.now().minusHours(24))) { "date is out of range, max 24h from now" }

            val pamImportPageable = mapPageable(page, number, size, sort)
            val slice = mapSlice(adPulsService.findByProviderIdAndUpdatedAfter(providerId, fromDate, pamImportPageable))
            LOG.debug("Returning json in getAllTodayStatsForProvider")
            ctx.status(HttpStatus.OK).json(slice)
            LOG.debug("Exiting getAllTodayStatsForProvider")
        } catch (e: Exception) {
            LOG.error("Error in getAllTodayStatsForProvider", e)
            throw e
        }
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
        LOG.info("Sort: " + sort)
        LOG.info("Filtered Sort: " + sort.filterNot { it == "updated" || it == "created" || it == "asc" || it == "desc" })
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
