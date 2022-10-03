package com.gildedrose.domain

import org.springframework.stereotype.Service

@Service
class GildedRose() {
    fun update(items: List<Item>) {
        items.forEach { item ->
            update(item)
        }
    }

    fun update(item: Item) {
        item.type.update(item)
    }
}

val Item.type
    get() = when (name) {
        "Backstage passes to a TAFKAL80ETC concert" -> PASS
        "Aged Brie" -> BRIE
        "Sulfuras, Hand of Ragnaros" -> SULFURAS
        else -> NORMAL
    }

open class ItemType {

    fun update(item: Item) {
        age(item)
        degrade(item)
    }

    protected open fun age(item: Item) {
        item.sellIn -= 1
    }

    protected open fun degrade(item: Item) {
        item.setQuality(
            degradation(item)
        )
    }

    protected open fun degradation(item: Item) = when {
        item.sellIn < 0 -> item.quality - 2
        else -> item.quality - 1
    }

    protected fun Item.setQuality(quality: Int) {
        this.quality = quality.coerceIn(0, 50)
    }
}

object PASS : ItemType() {
    override fun degrade(item: Item) {
        item.setQuality(
            degradation(item)
        )
    }

    override fun degradation(item: Item) = when {
        item.sellIn < 0 -> 0
        item.sellIn < 5 -> item.quality + 3
        item.sellIn < 10 -> item.quality + 2
        else -> item.quality + 1
    }
}

object BRIE : ItemType() {
    override fun degrade(item: Item) {
        item.setQuality(
            degradation(item)
        )
    }

    override fun degradation(item: Item) = if (item.sellIn < 0) {
        item.quality + 2
    } else
        item.quality + 1
}

object SULFURAS : ItemType() {
    override fun age(item: Item) {}
    override fun degrade(item: Item) {}
}

object NORMAL : ItemType()