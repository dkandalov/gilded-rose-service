package com.gildedrose

import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED

class WebControllerIntegrationTest {
    private val template = TestRestTemplate(RestTemplateBuilder().rootUri("http://127.0.0.1:8081/"))
    private val itemsRepository = mock<ItemsRepository>()
    private val config = Config.load("test")
    private val server = WebControllerHttp4k(config, GildedRoseService(itemsRepository))
        .asServer(Undertow(config.port)).start()

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    @Test
    fun `get items for a date`() {
        given(itemsRepository.loadItems(any())).willReturn(listOf(
            Pair(LocalDate(2019, 1, 2), Item(name = "Box", sellIn = 1, quality = 2))
        ))

        val response = template.withBasicAuth("testUser", "secret")
            .getForEntity("/items?date=2019-01-02", String::class.java)
        assertThat(response.body).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }

    @Test
    fun `get items for a date with no items`() {
        given(itemsRepository.loadItems(any())).willReturn(emptyList())

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