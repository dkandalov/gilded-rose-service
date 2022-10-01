package com.gildedrose

import com.gildedrose.domain.GildedRose
import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired

class GildedRoseService(
    private val repository: ItemsRepository,
    private val gildedRose: GildedRose = GildedRose()
) {
    @Autowired private val logger: Logger? = null

    fun items(asOfDate: LocalDate): List<Item> {
        logger?.info("Loading items for $asOfDate")

        val items = repository.loadItems(createdOnOrBefore = asOfDate)
        return items.map { (createdDate, item) ->
            repeat(times = (asOfDate - createdDate).days) {
                gildedRose.update(item)
            }
            item
        }
    }
}
