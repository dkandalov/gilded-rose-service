package com.gildedrose

import kotlinx.datetime.LocalDate
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
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
    private val logger: Logger,
    private val gildedRoseService: GildedRoseService = GildedRoseService(
        DbItemsRepository(config.db.toDataSource()),
        logger
    ),
) : HttpHandler {

    private val basicAuth = BasicAuth("") { credentials ->
        credentials.user in config.users && credentials.password == "${credentials.user}-pass"
    }
    private val date = Query.map(kotlinxLocalDate).required("date")
    private val body = Body.auto<List<Item>>().toLens()

    private val routes = Filter.NoOp
        .then(basicAuth)
        .then(CatchLensFailure())
        .then(
            routes(
                "items" bind GET to { request ->
                    val date: LocalDate = date(request)
                    logger.info("Requested items for $date")
                    val items = gildedRoseService.items(date)
                    Response(OK).with(body of items)
                }
            )
        )

    override fun invoke(request: Request) = routes(request)
}

private val kotlinxLocalDate = BiDiMapping<String, LocalDate>(
    { LocalDate.parse(it) },
    { it.toString() }
)