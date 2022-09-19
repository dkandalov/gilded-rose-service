package com.gildedrose

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableConfigurationProperties(value = [Config::class])
class LoggingConfig {
    @Bean
    @Scope("prototype")
    fun log(injectionPoint: InjectionPoint): Logger {
        val name = injectionPoint.field?.declaringClass ?: injectionPoint.member.declaringClass
        return LoggerFactory.getLogger(name)
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
                .password("$userName-pass")
                .roles("USER")
                .build()
        }
        return InMemoryUserDetailsManager(users)
    }
}