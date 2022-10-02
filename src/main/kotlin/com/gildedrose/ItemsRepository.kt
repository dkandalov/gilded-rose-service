package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import javax.sql.DataSource

interface ItemsRepository {
    fun loadItems(createdOnOrBefore: LocalDate): List<Pair<LocalDate, Item>>
}

@Repository
class DbItemsRepository(
    @Autowired private val dataSource: DataSource
) : ItemsRepository {
    private val jdbc = JdbcTemplate(dataSource)
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
        return jdbc.query(
            "select * from Items where createdOn <= ?",
            rowMapper,
            createdOnOrBefore.toString()
        )
    }
}
