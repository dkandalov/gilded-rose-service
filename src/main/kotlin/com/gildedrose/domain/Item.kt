package com.gildedrose.domain

data class Item(
    val name: String,
    var sellIn: Int,
    var quality: Int
) {
    override fun toString() = "$name, $sellIn, $quality"
}