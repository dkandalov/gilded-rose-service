package com.gildedrose

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*
import kotlin.concurrent.thread

fun main() {
    val app = App(Config.load()).start()
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        app.close()
    })
}

class App (config: Config) : AutoCloseable {
    val dataSource = config.db.toDataSource()
    private val itemsRepository = DbItemsRepository(dataSource)
    private val gildedRoseService = GildedRoseService(itemsRepository)
    private val controller = WebControllerHttp4k(config, gildedRoseService)
    private val server = controller.asServer(Undertow(config.port))

    fun start() = apply {
        server.start()
    }

    override fun close() {
        server.close()
        dataSource.close()
    }
}

@SpringBootApplication
class GildedRoseApplication

@ConfigurationProperties(prefix = "gildedrose")
class Config(
    var users: List<String> = emptyList(),
    var port: Int = 0,
    var db: DbConfig = DbConfig()
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
