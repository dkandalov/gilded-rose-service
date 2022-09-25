package com.gildedrose

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.slf4j.Logger

class GildedRoseService(
    private val repository: ItemsRepository,
    private val logger: Logger,
    private val gildedRose: GildedRose = GildedRose()
) {
    fun items(asOfDate: LocalDate): List<Item> {
        logger.info("Loading items for $asOfDate")
        val updatedItems = repository.loadItems(createdOnOrBefore = asOfDate).map { (createdDate, item) ->
            repeat(times = asOfDate.minus(createdDate).days) {
                logger.info("Updating item $item")
                gildedRose.update(item)
            }
            item
        }
        return updatedItems
    }
}
