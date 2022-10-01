package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.slf4j.Logger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import javax.sql.DataSource

interface ItemsRepository {
    fun loadItems(createdOnOrBefore: LocalDate): List<Pair<LocalDate, Item>>
}

class DbItemsRepository(
    dataSource: DataSource,
    newLogger: (String) -> Logger = ::defaultLogger
) : ItemsRepository {
    private val jdbcTemplate = JdbcTemplate(dataSource)
    private val logger = newLogger(javaClass.simpleName)

    override fun loadItems(createdOnOrBefore: LocalDate): List<Pair<LocalDate, Item>> {
        val rowMapper = RowMapper { resultSet, _ ->
            val item = Item(
                name = resultSet.getString(1),
                sellIn = resultSet.getInt(2),
                quality = resultSet.getInt(3)
            )
            val createdDate = LocalDate.parse(resultSet.getString(4))
            logger.info("Loaded item $item")
            Pair(createdDate, item)
        }
        return jdbcTemplate.query(
            "select * from Items where createdOn <= ?",
            rowMapper,
            createdOnOrBefore.toString()
        )
    }
}
