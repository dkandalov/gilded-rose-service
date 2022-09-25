package com.gildedrose

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters.BasicAuth
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.servlet.HttpHandlerServlet
import org.http4k.servlet.asServlet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.ServletRegistrationBean
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
import org.springframework.web.bind.annotation.RestController

class WebControllerHttp4k(
    private val config: Config,
    private val gildedRoseService: GildedRoseService = GildedRoseService(DbItemsRepository(config.db.toDataSource())),
) : HttpHandler {
    private val objectMapper = jacksonObjectMapper()

    val routes = routes(
        "items" bind GET to { request ->
            val items = request.query("date")?.let { gildedRoseService.items(it.toLocalDate()) }
            if (items == null) Response(BAD_REQUEST).body("date parameter is required")
            else Response(OK).header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(items))
        }
    ).withFilter(BasicAuth("") { it.user in config.users && it.password == "${it.user}-pass" })

    override fun invoke(request: Request) = routes(request)
}

@RestController
class WebController

@Configuration
@EnableWebSecurity
class WebSecurityConfig(@Autowired private val config: Config) {

    @Bean fun http4kServletRegistrationBean(): ServletRegistrationBean<HttpHandlerServlet> {
        val servlet = WebControllerHttp4k(config).routes.asServlet()
        return ServletRegistrationBean<HttpHandlerServlet>(servlet, "/items/*")
    }

    @Bean fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic()
        return http.build()
    }

    @Bean fun userDetailsService(): UserDetailsService {
        val users = config.users.map { userName ->
            @Suppress("DEPRECATION")
            User.withDefaultPasswordEncoder()
                .username(userName)
                .password("$userName-pass")
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
