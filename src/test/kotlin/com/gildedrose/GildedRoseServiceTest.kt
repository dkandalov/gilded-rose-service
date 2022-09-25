package com.gildedrose

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.slf4j.LoggerFactory

class GildedRoseServiceTest {
    private val itemsRepository = mock<ItemsRepository>()
    private val service = GildedRoseService(itemsRepository, testLogger)

    @BeforeEach
    fun setup() {
        given(itemsRepository.loadItems(any())).willReturn(listOf(
            Pair(LocalDate.parse("2021-01-02"), Item("Box", 10, 20))
        ))
    }

    @Test fun `items which were added on the same date`() {
        val items = service.items(LocalDate.parse("2021-01-02"))
        assertEquals(listOf(Item("Box", 10, 20)), items)
    }

    @Test fun `items which were added day before`() {
        val items = service.items(LocalDate.parse("2021-01-04"))
        assertEquals(listOf(Item("Box", 8, 18)), items)
    }
}

val testLogger = LoggerFactory.getLogger("testLogger")