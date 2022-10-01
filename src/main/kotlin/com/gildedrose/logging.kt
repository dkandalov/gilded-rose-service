package com.gildedrose

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun defaultLogger(name: String): Logger =
    LoggerFactory.getLogger(name)
