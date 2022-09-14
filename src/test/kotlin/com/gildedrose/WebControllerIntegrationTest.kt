package com.gildedrose

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebControllerIntegrationTest {
    @Autowired private lateinit var rest: TestRestTemplate
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
        jdbcTemplate.createItemsTable()
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.dropItemsTable()
    }

    @Test
    fun `get items for a date`() {
        jdbcTemplate.execute(
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