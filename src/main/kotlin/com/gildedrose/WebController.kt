package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@RestController
class WebController(
    @Autowired dataSource: DataSource,
    repository: ItemsRepository = DbItemsRepository(dataSource)
) {
    private val gildedRoseService: GildedRoseService = GildedRoseService(repository)

    @GetMapping("/items")
    fun items(@RequestParam date: LocalDate): List<Item> {
        return gildedRoseService.items(date)
    }
}

