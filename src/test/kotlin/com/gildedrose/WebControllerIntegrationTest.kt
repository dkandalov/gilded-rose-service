package com.gildedrose

import org.assertj.core.api.Assertions.assertThat
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.jdbc.core.JdbcTemplate

class WebControllerIntegrationTest {
    private val app = App(env = "test")
    private val jdbc = JdbcTemplate(app.dataSource)
    private val rest = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:${app.config.port}/"))

    @BeforeEach
    fun setup() {
        app.start()
        jdbc.createItemsTable()
    }

    @AfterEach
    fun tearDown() {
        jdbc.dropItemsTable()
        app.stop()
    }

    @Test
    fun `get items for a date`() {
        jdbc.execute(
            "insert into Items (name, sellIn, quality, createdOn) values('Box', 1, 2, '2019-01-02')"
        )

        val response = rest.withBasicAuth("testUser", "secret")
            .getForEntity("/items?date=2019-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }

    @Test
    fun `get items for a date with no items`() {
        val response = rest.withBasicAuth("testUser", "secret")
            .getForEntity("/items?date=2019-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[]""")
    }

    @Test
    fun `authentication is required to get items`() {
        val response = rest
            .withBasicAuth("invalidUser", "pass")
            .getForEntity("/items", String::class.java)
        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `date is required to get items`() {
        val response = rest.withBasicAuth("testUser", "secret")
            .getForEntity("/items", String::class.java)
        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
    }
}