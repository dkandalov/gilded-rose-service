package com.gildedrose

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.jdbc.core.JdbcTemplate

class IntegrationTest {
    private val rest = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:8081/"))
    private val config = loadConfig("test")
    private val jdbcTemplate = JdbcTemplate(config.db.toDataSource())
    private val app = startApp(config)

    @BeforeEach
    fun setup() {
        jdbcTemplate.createItemsTable()
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.dropItemsTable()
        app.close()
    }

    @Test
    fun `get items for a date`() {
        jdbcTemplate.execute("insert into Items (name, sellIn, quality, createdOn) values('Box', 1, 2, '2021-01-02');")

        val response = rest
            .withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items?date=2021-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }

    @Test
    fun `get items for a date with no items`() {
        val response = rest
            .withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items?date=2021-01-02", String::class.java)
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
        val response = rest
            .withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items", String::class.java)
        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
    }
}