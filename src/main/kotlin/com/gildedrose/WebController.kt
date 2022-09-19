package com.gildedrose

import kotlinx.datetime.LocalDate
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WebController(
    @Autowired val gildedRoseService: GildedRoseService,
    @Autowired val logger: Logger
) {
    @GetMapping("/items")
    fun items(@RequestParam date: LocalDate): List<Item> {
        logger.info("Requested items for $date")
        return gildedRoseService.items(date)
    }
}

@Component
class TokenConverter : Converter<String, LocalDate> {
    override fun convert(isoString: String) = LocalDate.parse(isoString)
}
