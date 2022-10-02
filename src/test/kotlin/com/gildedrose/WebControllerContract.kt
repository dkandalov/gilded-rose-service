package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.OkHttp
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.jdbc.core.JdbcTemplate

class IntegrationTest : WebControllerContract {
    private val app = App("test")
    private val jdbcTemplate = JdbcTemplate(app.dataSource)
    override val handler = SetBaseUriFrom(Uri.of("http://127.0.0.1:${app.config.port}/")).then(OkHttp())

    @BeforeEach
    fun start() {
        app.start()
        jdbcTemplate.createItemsTable()
        jdbcTemplate.execute("insert into Items (name, sellIn, quality, createdOn) values('Box', 1, 2, '2019-01-02');")
    }

    @AfterEach
    fun stop() {
        jdbcTemplate.dropItemsTable()
        app.stop()
    }
}

class WebControllerTest : WebControllerContract {
    private val itemsRepository = mock<ItemsRepository>()
    override val handler = WebController(
        Config.load("test"),
        GildedRoseService(itemsRepository)
    )

    @BeforeEach
    fun setup() {
        given(itemsRepository.loadItems(eq(LocalDate(2019, 3, 2)))).willReturn(listOf(
            Pair(LocalDate(2019, 1, 2), Item(name = "Box", sellIn = 1, quality = 2))
        ))
    }
}

interface WebControllerContract {
    val handler: HttpHandler

    @Test
    fun `get items for a date`() {
        val response = handler(Request(GET, "/items").query("date", "2019-03-02")
            .withBasicAuth(Credentials("testUser", "secret")))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.bodyString()).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }
    @Test
    fun `get items for a date with no items`() {
        val response = handler(Request(GET, "/items").query("date", "2018-01-02")
            .withBasicAuth(Credentials("testUser", "secret")))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.bodyString()).isEqualTo("""[]""")
    }

    @Test
    fun `authentication is required to get items`() {
        val response = handler(Request(GET, "/items").query("date", "2019-01-02")
            .withBasicAuth(Credentials("invalidUser", "invalidPass")))

        assertThat(response.status).isEqualTo(UNAUTHORIZED)
        assertThat(response.bodyString()).isEqualTo("")
    }

    @Test
    fun `date is required to get items`() {
        val response = handler(Request(GET, "/items")
            .withBasicAuth(Credentials("testUser", "secret")))

        assertThat(response.status).isEqualTo(BAD_REQUEST)
    }
}
