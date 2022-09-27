package com.gildedrose

import org.http4k.core.Uri
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

class InProcessIntegrationTest : IntegrationContract {
    val config = loadConfig("test")
    override val jdbcTemplate = JdbcTemplate(config.db.toDataSource())
    override val handler = WebController(config, LoggerFactory.getLogger("GildedRose"))
    override val baseUri = Uri.of("/")
}


