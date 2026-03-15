package com.jmarfildev.rockalendar.geo.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProvinceApiContractTest extends AbstractPostgresTest {

    @Autowired
    MockMvc mockMvc;

    private final String API_PROVINCES_COMBO = "/api/provinces/combo";

    @Test
    @DisplayName("GET /api/provinces/combo (público) -> 200 con lista no vacía de provincias")
    void listCombo_public_returns200WithProvinces() throws Exception {
        mockMvc.perform(get(API_PROVINCES_COMBO))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", not(empty())));
    }
}
