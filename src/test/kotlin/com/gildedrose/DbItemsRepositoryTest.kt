package com.gildedrose

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate


class DbItemsRepositoryTest {
    private val dataSource = loadConfig("test").db.toDataSource()
    private val jdbcTemplate = JdbcTemplate(dataSource)
    private val repository = DbItemsRepository(dataSource)

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("create table Items(name varchar(255), sellIn int, quality int, createdOn varchar(255))")
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.execute("drop table Items")
        dataSource.close()
    }

    @Test fun `load items on or before specified date`() {
        jdbcTemplate.execute("""
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-01');
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-02');
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-03');
            """.trimIndent()
        )

        assertEquals(
            listOf(
                Pair(LocalDate(2021, 1, 1), Item("Box", 10, 20)),
                Pair(LocalDate(2021, 1, 2), Item("Box", 10, 20))
            ),
            repository.loadItems(createdOnOrBefore = LocalDate(2021, 1, 2))
        )
    }

    @Test fun `load items on or before specified date 2`() {
        jdbcTemplate.execute("""
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-01');
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-02');
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2021-01-03');
            """.trimIndent()
        )

        assertEquals(
            listOf(
                Pair(LocalDate(2021, 1, 1), Item("Box", 10, 20)),
                Pair(LocalDate(2021, 1, 2), Item("Box", 10, 20))
            ),
            repository.loadItems(createdOnOrBefore = LocalDate(2021, 1, 2))
        )
    }
}
