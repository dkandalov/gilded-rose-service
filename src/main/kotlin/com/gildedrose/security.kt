package com.gildedrose

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Autowired
    private val config: Config? = null

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic()
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
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