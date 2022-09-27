package com.gildedrose

import org.http4k.client.ApacheClient
import org.http4k.core.Uri
import org.junit.jupiter.api.AfterEach
import org.springframework.jdbc.core.JdbcTemplate

class AppIntegrationTest : IntegrationContract {
    private val config = loadConfig("test")
    override val jdbcTemplate = JdbcTemplate(config.db.toDataSource())
    override val handler = ApacheClient()
    override val baseUri: Uri = Uri.of("http://localhost:${config.port}")

    private val app = startApp(config)

    @AfterEach fun stopApp() {
        app.close()
    }
}


