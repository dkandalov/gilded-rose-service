package com.gildedrose

import com.gildedrose.domain.GildedRose
import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GildedRoseService(
    private val repository: ItemsRepository,
    private val gildedRose: GildedRose = GildedRose()
) {
    private val logger = newLogger()

    fun items(asOfDate: LocalDate): List<Item> {
        logger.info("Loading items for $asOfDate")

        val items = repository.loadItems(createdOnOrBefore = asOfDate)
        return items.map { (createdDate, item) ->
            repeat(times = (asOfDate - createdDate).days) {
                gildedRose.update(item)
            }
            item
        }
    }
}
