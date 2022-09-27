package com.gildedrose

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

fun main() {
    startApp(Config.load())
}

fun startApp(config: Config): AutoCloseable {
    val dataSource = config.db.toDataSource()
    val gildedRoseService = GildedRoseService(DbItemsRepository(dataSource))
    val server = WebController(config, gildedRoseService)
        .asServer(Undertow(config.port))
        .start()
    return AutoCloseable {
        server.close()
        dataSource.close()
    }
}

class Config(
    val users: List<String>,
    val port: Int,
    val db: DbConfig
) {
    companion object {
        fun load(env: String? = null): Config {
            val envPostfix = if (env == null) "" else "-$env"
            val properties = propertiesFromClasspath("/application$envPostfix.properties")
            return Config(
                users = properties["gildedrose.users"].toString().split(","),
                port = properties["server.port"].toString().toInt(),
                db = DbConfig(
                    url = properties["spring.datasource.url"].toString(),
                    username = properties["spring.datasource.username"].toString(),
                    password = properties["spring.datasource.password"].toString()
                )
            )
        }
    }
}

class DbConfig(
    val url: String,
    val username: String,
    val password: String
)

fun DbConfig.toDataSource() =
    HikariDataSource(
        HikariConfig().also {
            it.jdbcUrl = url
            it.username = username
            it.password = password
        }
    )

private fun propertiesFromClasspath(path: String) = Properties().apply {
    load(Config::class.java.getResourceAsStream(path))
}

fun defaultLogger(name: String): Logger = LoggerFactory.getLogger(name)
