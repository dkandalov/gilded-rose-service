package com.gildedrose

import kotlinx.datetime.LocalDate
import org.h2.jdbcx.JdbcDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import javax.sql.DataSource


interface ItemsRepository {
    fun loadItems(createdOnOrBefore: LocalDate): List<Pair<LocalDate, Item>>
}

class DbItemsRepository(
    private val dataSource: DataSource = inMemoryDatasource(),
    private val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)
) : ItemsRepository {

    override fun loadItems(createdOnOrBefore: LocalDate): List<Pair<LocalDate, Item>> {
        val sql = "select * from Items where createdOn <= ?"
        return jdbcTemplate.query(sql, rowMapper, createdOnOrBefore.toString())
    }

    private val rowMapper = RowMapper { resultSet, _ ->
        val item = Item(
            name = resultSet.getString(1),
            sellIn = resultSet.getInt(2),
            quality = resultSet.getInt(3)
        )
        val createdDate = LocalDate.parse(resultSet.getString(4))
        Pair(createdDate, item)
    }
}

fun inMemoryDatasource() =
    JdbcDataSource().also { it.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1") }
