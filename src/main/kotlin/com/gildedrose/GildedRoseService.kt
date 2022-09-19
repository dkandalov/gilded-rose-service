package com.gildedrose

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GildedRoseService(
    private val repository: ItemsRepository = DbItemsRepository(),
    private val gildedRose: GildedRose = GildedRose(),
    private val logger: Logger = LoggerFactory.getLogger("GildedRoseService")
) {
    fun items(asOfDate: LocalDate): List<Item> {
        logger.info("Loading items for $asOfDate")
        val updatedItems = repository.loadItems(createdOnOrBefore = asOfDate)
            .map { (createdDate, item) ->
                repeat(times = asOfDate.minus(createdDate).days) {
                    logger.info("updating item")
                    gildedRose.update(item)
                }
                item
            }
        return updatedItems
    }
}
