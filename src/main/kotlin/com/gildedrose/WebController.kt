package com.gildedrose

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.datetime.toLocalDate
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.Logger

class WebController(
    private val config: Config,
    private val gildedRoseService: GildedRoseService,
    newLogger: (String) -> Logger = ::defaultLogger
) : HttpHandler {
    private val logger = newLogger(javaClass.simpleName)
    private val objectMapper = jacksonObjectMapper()

    private val routes = routes(
        "items" bind GET to { request ->
            val items = request.query("date")?.let {
                logger.info("Requested items for $it")
                gildedRoseService.items(it.toLocalDate())
            }
            if (items == null) Response(BAD_REQUEST).body("date parameter is required")
            else Response(OK).header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(items))
        }
    ).withFilter(BasicAuth("") { it.user in config.users && it.password == "secret" })

    override fun invoke(request: Request) = routes(request)
}
