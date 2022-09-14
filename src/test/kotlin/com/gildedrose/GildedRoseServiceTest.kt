package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
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
            Pair(LocalDate(2019, 1, 2), Item("Box", 10, 20)),
            Pair(LocalDate(2019, 1, 2), Item("Aged Brie", 20, 30)),
        ))
    }

    @Test
    fun `items which were added on the same date`() {
        val items = service.items(LocalDate(2019, 1, 2))
        assertThat(items).isEqualTo(listOf(
            Item("Box", 10, 20),
            Item("Aged Brie", 20, 30)
        ))
    }

    @Test
    fun `items which were added day before`() {
        val items = service.items(LocalDate(2019, 1, 4))
        assertThat(items).isEqualTo(listOf(
            Item("Box", 8, 18),
            Item("Aged Brie", 18, 32)
        ))
    }
}