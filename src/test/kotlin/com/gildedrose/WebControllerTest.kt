package com.gildedrose

import com.gildedrose.domain.Item
import kotlinx.datetime.LocalDate
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @MockBean
    private lateinit var itemsRepository: ItemsRepository

    @Test
    fun `context loads`() {
    }

    @Test
    fun `get items for a date`() {
        given(itemsRepository.loadItems(any())).willReturn(listOf(
            Pair(LocalDate(2019, 1, 2), Item(name = "Box", sellIn = 1, quality = 2))
        ))

        mvc.perform(get("/items?date=2019-03-02").accept(APPLICATION_JSON).with(user("someUser")))
            .andExpect(status().isOk)
            .andExpect(content().string(equalTo("""[{"name":"Box","sellIn":1,"quality":2}]""")))
    }

    @Test
    fun `get items for a date with no items`() {
        given(itemsRepository.loadItems(any())).willReturn(emptyList())

        mvc.perform(get("/items?date=2019-01-02").accept(APPLICATION_JSON).with(user("someUser")))
            .andExpect(status().isOk)
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
            .with(user("someUser")))
            .andExpect(status().isBadRequest)
    }
}
