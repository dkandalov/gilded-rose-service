package com.gildedrose

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

fun newLogger(name: String?): Logger = LoggerFactory.getLogger(name)
fun Any.newLogger() = newLogger(this.javaClass.simpleName)
