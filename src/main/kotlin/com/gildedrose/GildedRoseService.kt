package com.gildedrose

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.slf4j.Logger
import org.springframework.stereotype.Service

@Service
class GildedRoseService(
    val repository: ItemsRepository = DbItemsRepository(Config.load().db.toDataSource()),
    val gildedRose: GildedRose = GildedRose(),
    newLogger: (String) -> Logger = ::defaultLogger
) {
    private val logger = newLogger(javaClass.simpleName)

    fun items(asOfDate: LocalDate): List<Item> {
        logger.info("Loading items for $asOfDate")
        val items = repository.loadItems(createdOnOrBefore = asOfDate)
        return items.map { (createdDate, item) ->
            repeat(times = asOfDate.minus(createdDate).days) {
                gildedRose.update(item)
            }
            item
        }
    }
}
