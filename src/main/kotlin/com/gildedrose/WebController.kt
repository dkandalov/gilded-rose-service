package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WebController(@Autowired val gildedRoseService: GildedRoseService) {
    @GetMapping("/items")
    fun items(@RequestParam date: LocalDate): List<Item> {
        return gildedRoseService.items(date)
    }
}

