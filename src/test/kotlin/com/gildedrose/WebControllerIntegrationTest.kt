package com.gildedrose

import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebControllerIntegrationTest {
    @Autowired
    private lateinit var template: TestRestTemplate
    @MockBean
    private lateinit var itemsRepository: ItemsRepository

    @Test
    fun `get items for a date`() {
        given(itemsRepository.loadItems(any())).willReturn(listOf(
            Pair(LocalDate(2021, 1, 2), Item("Box", 1, 2))
        ))

        val response = template.withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items?date=2021-01-02", String::class.java)

        assertThat(response.body).isEqualTo("""[{"name":"Box","sellIn":1,"quality":2}]""")
    }

    @Test
    fun `get items for a date with no items`() {
        given(itemsRepository.loadItems(any())).willReturn(emptyList())

        val response = template.withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items?date=2021-01-02", String::class.java)

        assertThat(response.body).isEqualTo("""[]""")
    }

    @Test
    fun `authentication is required to get items`() {
        val response = template.withBasicAuth("invalidUser", "pass")
            .getForEntity("/items", String::class.java)

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `date is required to get items`() {
        val response = template.withBasicAuth("testUser", "testUser-pass")
            .getForEntity("/items", String::class.java)

        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
    }
}