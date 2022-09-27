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

class IntegrationTest {
    private val template = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:8081/"))
    private val config = Config.load("test")
    private val jdbcTemplate = JdbcTemplate(config.db.toDataSource())
    private val app =
        WebControllerHttp4k(config, GildedRoseService(DbItemsRepository(config.db.toDataSource())))
            .asServer(Undertow(config.port))
            .start()

    @BeforeEach
    fun createTable() {
        jdbcTemplate.execute("create table Items(name varchar(255), sellIn int, quality int, createdOn varchar(255))")
    }

    @AfterEach
    fun tearDown() {
        app.stop()
        jdbcTemplate.execute("drop table Items")
    }

    @Test
    fun `get items for a date`() {
        jdbcTemplate.execute("insert into Items (name, sellIn, quality, createdOn) values('Box', 1, 2, '2019-01-02');")

        val response = template.withBasicAuth("testUser", "secret")
            .getForEntity("/items?date=2019-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }

    @Test
    fun `get items for a date with no items`() {
        val response = template.withBasicAuth("testUser", "secret")
            .getForEntity("/items?date=2019-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[]""")
    }

    @Test
    fun `authentication is required to get items`() {
        val response = template
            .withBasicAuth("invalidUser", "pass")
            .getForEntity("/items", String::class.java)
        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `date is required to get items`() {
        val response = template.withBasicAuth("testUser", "secret")
            .getForEntity("/items", String::class.java)
        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
    }
}