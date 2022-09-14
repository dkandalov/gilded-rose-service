package com.gildedrose

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ItemsRepositoryImplTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    private lateinit var repository: ItemsRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("create table Items(name varchar(255), sellIn int, quality int, createdOn varchar(255))")
        jdbcTemplate.execute("""
            insert into Items (name, sellIn, quality, createdOn) values('Box', 10, 20, '2019-01-01');
            insert into Items (name, sellIn, quality, createdOn) values('Aged Brie', 30, 40, '2019-01-02');
            insert into Items (name, sellIn, quality, createdOn) values('Conjured Mana Cake', 50, 60, '2019-01-03');
            """
        )
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.execute("drop table Items")
    }

    @Test
    fun `load items added on or before specified date`() {
        assertEquals(
            listOf(
                Pair(LocalDate(2019, 1, 1), Item("Box", 10, 20)),
                Pair(LocalDate(2019, 1, 2), Item("Aged Brie", 30, 40))
            ),
            repository.loadItems(createdOnOrBefore = LocalDate(2019, 1, 2))
        )
    }

    @Test
    fun `load no items when they're added after specified date`() {
        assertEquals(
            emptyList<Pair<LocalDate, Item>>(),
            repository.loadItems(createdOnOrBefore = LocalDate(2018, 1, 1))
        )
    }
}