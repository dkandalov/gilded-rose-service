package com.gildedrose

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Credentials
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

class Http4kIntegrationTest {
    private val config = loadConfig("test")
    private val jdbcTemplate = JdbcTemplate(config.db.toDataSource())
    private val handler = WebController(config, LoggerFactory.getLogger("GildedRose"))

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("create table Items(name varchar(255), sellIn int, quality int, createdOn varchar(255))")
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.execute("drop table Items")
    }

    @Test
    fun `get items for a date`() {
        jdbcTemplate.execute("insert into Items (name, sellIn, quality, createdOn) values('Box', 1, 2, '2021-01-02');")

        val request = Request(GET, "/items?date=2021-01-02").withBasicAuth(Credentials("testUser", "testUser-pass"))
        assertThat(
            handler(request),
            hasStatus(OK) and hasBody("""[{"name":"Box","sellIn":1,"quality":2}]""")
        )
    }

    @Test
    fun `get items for a date with no items`() {
        val request = Request(GET, "/items?date=2021-01-02").withBasicAuth(Credentials("testUser", "testUser-pass"))
        assertThat(
            handler(request),
            hasStatus(OK) and hasBody("""[]""")
        )
    }

    @Test
    fun `authentication is required to get items`() {
        val request = Request(GET, "/items?date=2021-01-02")
            .withBasicAuth(Credentials("invalidUser", "pass"))
        assertThat(handler(request), hasStatus(UNAUTHORIZED) and hasBody(""))
    }

    @Test
    fun `date is required to get items`() {
        val request = Request(GET, "/items")
            .withBasicAuth(Credentials("testUser", "testUser-pass"))
        assertThat(handler(request), hasStatus(BAD_REQUEST))
    }
}

private fun Request.withBasicAuth(credentials: Credentials) = withBasicAuth(credentials, "Authorization")

