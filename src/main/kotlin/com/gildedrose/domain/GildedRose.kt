package com.gildedrose.domain

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GildedRose {
    @Autowired private val logger: Logger? = null

    fun update(items: List<Item>) {
        logger?.info("Updating items ${items.size}")

        items.forEach { item ->
            update(item)
        }
    }

    fun update(item: Item) {
        logger?.info("Updating item $item")

        when (item.name) {
            "Aged Brie"                                 -> item.updateQuality(1)
            "Backstage passes to a TAFKAL80ETC concert" ->
                if (item.sellIn <= 5) item.updateQuality(3)
                else if (item.sellIn <= 10) item.updateQuality(2)
                else item.updateQuality(1)
            "Sulfuras, Hand of Ragnaros"                -> item.updateQuality(0)
            else                                        -> item.updateQuality(-1)
        }

        item.sellIn -= if (item.name == "Sulfuras, Hand of Ragnaros") 0 else 1

        if (item.sellIn < 0) {
            when (item.name) {
                "Aged Brie"                                 -> item.updateQuality(1)
                "Backstage passes to a TAFKAL80ETC concert" -> item.updateQuality(-item.quality)
                "Sulfuras, Hand of Ragnaros"                -> item.updateQuality(0)
                else                                        -> item.updateQuality(-1)
            }
        }
    }

    private fun Item.updateQuality(change: Int) {
        if (change == 0) return
        quality = (quality + change).coerceIn(0, 50)
    }
}
