package com.gildedrose

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

fun main() {
    App().start()
}

class App(env: String? = null) {
    val config = Config.load(env)
    val dataSource = config.dbConfig.toDataSource()
    private val repository = DbItemsRepository(dataSource)
    private val service = GildedRoseService(repository)
    private val webController = WebController(config, service)
    private val server = webController.asServer(Undertow(config.port))

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
        dataSource.close()
    }
}

@SpringBootApplication
class GildedRoseApplication

@ConfigurationProperties(prefix = "gildedrose")
class Config(
    var users: List<String> = emptyList(),
    var port: Int = 0,
    var dbConfig: DbConfig = DbConfig()
) {
    companion object {
        fun load(env: String? = null): Config {
            val envPostfix = if (env == null) "" else "-$env"
            val properties = propertiesFromClasspath("/application$envPostfix.properties")
            return Config(
                users = properties["gildedrose.users"].toString().split(","),
                port = properties["server.port"].toString().toInt(),
                dbConfig = DbConfig(
                    url = properties["spring.datasource.url"].toString(),
                    username = properties["spring.datasource.username"].toString(),
                    password = properties["spring.datasource.password"].toString()
                )
            )
        }
    }
}

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

private fun propertiesFromClasspath(path: String) = Properties().apply {
    load(Config::class.java.getResourceAsStream(path))
}
