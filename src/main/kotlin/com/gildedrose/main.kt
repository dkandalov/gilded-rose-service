package com.gildedrose

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    runApplication<GildedRoseApplication>(*args)
}

@SpringBootApplication
class GildedRoseApplication

@ConfigurationProperties(prefix = "gildedrose")
class Config(var users: List<String> = emptyList())
