package com.gildedrose

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.FileInputStream
import java.util.*

fun main() {
    startApp(loadConfig())
}

fun startApp(config: Config): AutoCloseable {
    return WebControllerHttp4k(config)
        .asServer(Undertow(config.port))
        .start()
}

fun loadConfig(env: String? = null): Config {
    val properties = Properties()
    val envPostfix = if (env == null) "" else "-$env"
    properties.load(FileInputStream("src/main/resources/application$envPostfix.properties"))
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

@SpringBootApplication
class GildedRoseApplication

@ConfigurationProperties(prefix = "gildedrose")
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