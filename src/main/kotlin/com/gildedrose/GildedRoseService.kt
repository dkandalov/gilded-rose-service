package com.gildedrose

import com.gildedrose.domain.GildedRose
import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.slf4j.Logger

class GildedRoseService(
    private val repository: ItemsRepository,
    private val gildedRose: GildedRose = GildedRose(),
    newLogger: (String) -> Logger = ::defaultLogger
) {
    private val logger = newLogger(javaClass.simpleName)

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
