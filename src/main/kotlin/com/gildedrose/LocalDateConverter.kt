package com.gildedrose

import kotlinx.datetime.LocalDate
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class LocalDateConverter : Converter<String, LocalDate> {
    override fun convert(isoString: String) = LocalDate.parse(isoString)
}