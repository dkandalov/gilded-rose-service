package com.gildedrose

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebControllerTest(
    @Autowired private val mvc: MockMvc,
    @Autowired @MockBean private val gildedRoseService: GildedRoseService
) {
    @Test
    fun `get items for a date`() {
        given(gildedRoseService.items(any())).willReturn(listOf(Item("Box", 1, 2)))

        mvc.perform(
            get("/items?date=2021-03-02").accept(APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.user("someUser"))
        ).andExpect(status().isOk)
            .andExpect(content().string(equalTo("""[{"name":"Box","sellIn":1,"quality":2}]""")))
    }

    @Test
    fun `get items for a date with no items`() {
        given(gildedRoseService.items(any())).willReturn(emptyList())

        mvc.perform(
            get("/items?date=2021-01-02").accept(APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.user("someUser"))
        ).andExpect(status().isOk)
            .andExpect(content().string(equalTo("""[]""")))
    }

    @Test
    fun `authentication is required to get items`() {
        mvc.perform(get("/items").accept(APPLICATION_JSON))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `date is required to get items`() {
        mvc.perform(get("/items").accept(APPLICATION_JSON)
            .with(SecurityMockMvcRequestPostProcessors.user("someUser")))
            .andExpect(status().isBadRequest)
    }
}
