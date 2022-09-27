package com.gildedrose

import org.springframework.jdbc.core.JdbcTemplate

fun JdbcTemplate.createItemsTable() {
    execute("create table Items(name varchar(255), sellIn int, quality int, createdOn varchar(255))")
}

fun JdbcTemplate.dropItemsTable() {
    execute("drop table Items")
}
