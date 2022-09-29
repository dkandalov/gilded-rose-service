package com.gildedrose

import kotlinx.datetime.LocalDate
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

@RestController
class WebController(
    val config: Config = Config.load(),
    val gildedRoseService: GildedRoseService = GildedRoseService(
        DbItemsRepository(config.db.toDataSource())
    )
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
