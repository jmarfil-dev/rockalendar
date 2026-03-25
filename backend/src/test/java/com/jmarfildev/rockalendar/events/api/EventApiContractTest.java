package com.jmarfildev.rockalendar.events.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.ContractApiTestUtils;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
class EventApiContractTest extends AbstractPostgresTest {

    @Autowired
    ContractApiTestUtils contractUtils;
    @Autowired
    TestDataFactory factory;
    @Autowired
    MockMvc mockMvc;

    private final String apiEvents = "/api/events";
    private final String apiEventsHome = "/api/events/home";
    private final String apiEventsId = "/api/events/%s";

    @Test
    @DisplayName("GET /api/events es público -> 200")
    void getEvents_isPublic_returns200() throws Exception {
        mockMvc.perform(get(apiEvents))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/events con dateFrom > dateTo -> 400 ProblemDetail")
    void getEvents_invalidDateRange_returns400ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiEvents)
                .param("dateFrom", TestDates.rangeEnd().toString())
                .param("dateTo", TestDates.rangeStart().toString()));

        contractUtils.expectProblemDetail(ra, 400, apiEvents);
    }

    @Test
    @DisplayName("GET /api/events con size demasiado grande -> 400 ProblemDetail")
    void getEvents_pageSizeTooLarge_returns400ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiEvents)
                .param("size", String.valueOf(10_000)));

        contractUtils.expectProblemDetail(ra, 400, apiEvents);
    }

    @Test
    @DisplayName("GET /api/events/home es público -> 200")
    void getEventsHome_isPublic_returns200() throws Exception {
        mockMvc.perform(get(apiEventsHome))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/events/home con size demasiado grande -> 400 ProblemDetail")
    void getEventsHome_pageSizeTooLarge_returns400ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiEventsHome)
                .param("size", String.valueOf(10_000)));

        contractUtils.expectProblemDetail(ra, 400, apiEventsHome);
    }

    @Test
    @DisplayName("GET /api/events/{id} es público -> 200 cuando existe")
    void getEventById_isPublic_returns200WhenExists() throws Exception {
        var eventM = factory.approvedMadridAgainstYou();

        mockMvc.perform(get(apiEventsId.formatted("{id}"), eventM.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/events/{id} -> 404 ProblemDetail cuando no existe")
    void getEventById_notFound_returns404ProblemDetail() throws Exception {
        var missingId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        var ra = mockMvc.perform(get(apiEventsId.formatted("{id}"), missingId));

        contractUtils.expectProblemDetail(ra, 404, apiEventsId.formatted(missingId));
    }

    @Test
    @DisplayName("GET /api/events/{id} con UUID inválido -> 400 ProblemDetail")
    void getEventById_invalidUuid_returns400ProblemDetail() throws Exception {
        var ra = mockMvc.perform(get(apiEventsId.formatted("{id}"), "no-es-un-uuid"));

        contractUtils.expectProblemDetail(ra, 400, apiEventsId.formatted("no-es-un-uuid"));
    }
}
