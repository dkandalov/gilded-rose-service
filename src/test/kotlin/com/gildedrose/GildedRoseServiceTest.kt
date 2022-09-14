package com.gildedrose

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class GildedRoseServiceTest {
    @Autowired
    private lateinit var service: GildedRoseService
    @MockBean
    private lateinit var itemsRepository: ItemsRepository

    @BeforeEach
    fun setup() {
        given(itemsRepository.loadItems(any())).willReturn(listOf(
            Pair(LocalDate(2021, 1, 2), Item("Box", 10, 20))
        ))
    }

    @Test fun `items which were added on the same date`() {
        val items = service.items(LocalDate(2021, 1, 2))
        assertEquals(listOf(Item("Box", 10, 20)), items)
    }

    @Test fun `items which were added day before`() {
        val items = service.items(LocalDate(2021, 1, 4))
        assertEquals(listOf(Item("Box", 8, 18)), items)
    }
}