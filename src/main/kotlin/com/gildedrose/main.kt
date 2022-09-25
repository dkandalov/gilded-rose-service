package com.gildedrose

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

fun main() {
    startApp(loadConfig())
}

fun startApp(config: Config): AutoCloseable =
    WebController(config, LoggerFactory.getLogger("GildedRose"))
        .asServer(Undertow(config.port))
        .start()

fun loadConfig(env: String? = null): Config {
    val properties = Properties()
    val envPostfix = if (env == null) "" else "-$env"
    File("src/main/resources/application$envPostfix.properties").takeIf { it.exists() }?.inputStream()?.let { properties.load(it) }
    File("src/test/resources/application$envPostfix.properties").takeIf { it.exists() }?.inputStream()?.let { properties.load(it) }
    return Config(
        users = properties["gildedrose.users"].toString().split(","),
        port = properties["server.port"].toString().toInt(),
        db = DbConfig(
            url = properties["gildedrose.db.url"].toString(),
            username = properties["gildedrose.db.username"].toString(),
            password = properties["gildedrose.db.password"].toString()
        )
    )
}

class Config(
    var users: List<String> = emptyList(),
    var port: Int = 0,
    var db: DbConfig = DbConfig()
)

class DbConfig(
    var url: String = "",
    var username: String = "",
    var password: String = ""
)

fun DbConfig.toDataSource() =
    HikariDataSource(
        HikariConfig().also {
            it.jdbcUrl = url
            it.username = username
            it.password = password
        }
    )