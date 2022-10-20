package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.filter.ServerFilters.CatchLensFailure
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiMapping
import org.http4k.lens.Query
import org.http4k.lens.map
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.Logger

class WebController(
    private val config: Config,
    gildedRoseService: GildedRoseService
) : HttpHandler {
    private val logger = newLogger()
    private val authFilter = BasicAuth(realm = "") { credentials ->
        credentials.user in config.users && credentials.password == "secret"
    }
    private val routes = authFilter.then(CatchLensFailure())
        .then(routes(
            "items" bind GET to listItems(gildedRoseService, logger)
        ))

    override fun invoke(request: Request) = routes(request)
}

private fun listItems(gildedRoseService: GildedRoseService, logger: Logger): HttpHandler {
    val dateLens = Query.map(kotlinxLocalDate).required("date")
    val bodyLens = Body.auto<List<Item>>().toLens()
    return { request ->
        val date = dateLens(request)
        logger.info("Requested items for $date")
        val items = gildedRoseService.items(date)
        Response(OK).with(bodyLens of items)
    }
}

private val kotlinxLocalDate = BiDiMapping<String, LocalDate>(
    asOut = { LocalDate.parse(it) },
    asIn = { it.toString() }
)
