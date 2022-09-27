package com.gildedrose

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

class WebControllerHttp4k(
    private val config: Config,
    private val logger: Logger,
    private val gildedRoseService: GildedRoseService =
        GildedRoseService(DbItemsRepository(config.db.toDataSource())),
) : HttpHandler {
    private val objectMapper = jacksonObjectMapper()

    private val routes = routes(
        "items" bind GET to { request ->
            val items = request.query("date")?.let {
                logger.info("Requested items for $it")
                gildedRoseService.items(it.toLocalDate())
            }
            if (items == null) Response(BAD_REQUEST).body("date parameter is required")
            else Response(OK).header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(items))
        }
    ).withFilter(BasicAuth("") { it.user in config.users && it.password == "secret" })

    override fun invoke(request: Request) = routes(request)
}

@RestController
class WebController(
    @Autowired val gildedRoseService: GildedRoseService
) {
    @Autowired
    private val logger: Logger? = null

    @GetMapping("/items")
    fun items(@RequestParam date: LocalDate): List<Item> {
        logger?.info("Requested items for $date")
        return gildedRoseService.items(date)
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Autowired
    private val config: Config? = null

    @Bean fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic()
        return http.build()
    }

    @Bean fun userDetailsService(): UserDetailsService {
        val users = config!!.users.map { userName ->
            @Suppress("DEPRECATION")
            User.withDefaultPasswordEncoder()
                .username(userName)
                .password("secret")
                .roles("USER")
                .build()
        }
        return InMemoryUserDetailsManager(users)
    }
}

@Component
class TokenConverter : Converter<String, LocalDate> {
    override fun convert(isoString: String) = LocalDate.parse(isoString)
}
