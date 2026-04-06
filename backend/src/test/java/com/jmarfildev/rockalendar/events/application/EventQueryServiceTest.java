package com.jmarfildev.rockalendar.events.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.error.NotFoundException;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.api.dto.EventPrivateListItemDto;
import com.jmarfildev.rockalendar.events.api.dto.EventPublicListItemDto;
import com.jmarfildev.rockalendar.events.api.mapper.EventMapper;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;
import com.jmarfildev.rockalendar.support.TestDates;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
class EventQueryServiceTest extends AbstractPostgresTest {

    @Autowired
    EventQueryService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;
    @Autowired
    EventMapper mapper;

    @MockitoBean
    CurrentUser currentUser;

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    /*
     * listHome
     */

    @Test
    @DisplayName("listHome: size demasiado grande -> 400 BadRequestException")
    void listHome_pageSizeTooLarge_throws() {
        Pageable pageable = PageRequest.of(0, 10_000);

        assertThatThrownBy(() -> service.listHome(pageable)).isInstanceOf(BadRequestException.class)
                                                            .hasMessage(ErrorConstants.PAGE_SIZE_TOO_LARGE);
    }

    @Test
    @DisplayName("listHome: ignora sort del pageable y ordena resultados por fecha")
    void listHome_ignoresExternalSort_ordersResultsByDate() {
        factory.approvedMadridAgainstYou(); // Fecha futura primera
        factory.approvedBarcelonaBoikot(); // Fecha futura segunda

        Pageable pageableWithSort = PageRequest.of(0, 20, Sort.by("city_name").descending());
        var page = service.listHome(pageableWithSort);

        /*
         *  Si usa el sort city_name, Barcelona va primero. Pero el service está desarrollado
         *  para ignorar este sort y poner por fecha, así que Madrid va primero.
         */
        assertThat(page.getContent()).hasSize(2)
                                     .extracting(EventPublicListItemDto::cityName)
                                     .containsExactly(TestConstants.MADRID, TestConstants.BARCELONA);
    }

    @Test
    @DisplayName("listHome: filtra eventos pasados -> devuelve Page vacío")
    void listHome_filtersPastEvents() {
        factory.approvedValenciaPast(); // Fecha pasada

        var page = service.listHome(PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
    }

    /*
     * searchPublic
     */

    @Test
    @DisplayName("searchPublic: dateFrom > dateTo -> 400 BadRequestException")
    void searchPublic_invalidDateRange_throws() {
        OffsetDateTime from = OffsetDateTime.parse(TestDates.rangeEnd().toString());
        OffsetDateTime to = OffsetDateTime.parse(TestDates.rangeStart().toString());
        var pageable = PageRequest.of(0, 20);
        var fromOpt = Optional.of(from);
        var toOpt = Optional.of(to);
        Optional<String> noQuery = Optional.empty();
        Optional<Short> noProvinceId = Optional.empty();
        Optional<String> noCity = Optional.empty();
        Optional<UUID> noArtistId = Optional.empty();

        assertThatThrownBy(() -> service.searchPublic(noQuery, fromOpt, toOpt, noProvinceId, noCity, noArtistId,
                                                      pageable)).isInstanceOf(BadRequestException.class)
                                                                .hasMessage(ErrorConstants.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("searchPublic: size demasiado grande -> 400 BadRequestException")
    void searchPublic_pageSizeTooLarge_throws() {
        var pageable = PageRequest.of(0, 10_000);
        Optional<String> noQuery = Optional.empty();
        Optional<OffsetDateTime> noDateFrom = Optional.empty();
        Optional<OffsetDateTime> noDateTo = Optional.empty();
        Optional<Short> noProvinceId = Optional.empty();
        Optional<String> noCity = Optional.empty();
        Optional<UUID> noArtistId = Optional.empty();

        assertThatThrownBy(() -> service.searchPublic(noQuery, noDateFrom, noDateTo, noProvinceId, noCity, noArtistId,
                                                      pageable)).isInstanceOf(BadRequestException.class)
                                                                .hasMessage(ErrorConstants.PAGE_SIZE_TOO_LARGE);
    }

    @Test
    @DisplayName("searchPublic: query en blanco -> no filtra por texto, filtra por ciudad")
    void searchPublic_blankQuery_returnsResults() {
        factory.approvedBarcelonaBoikot();
        factory.approvedMadridAgainstYou();

        var page = service.searchPublic(Optional.of("   "), Optional.empty(), Optional.empty(), Optional.empty(),
                                        Optional.of(TestConstants.MADRID), Optional.empty(), PageRequest.of(0, 10));

        assertThat(page.getContent()).singleElement().extracting(EventPublicListItemDto::cityName).isEqualTo(TestConstants.MADRID);
    }

    @Test
    @DisplayName("searchPublic: query en blanco -> filtra por artistId y devuelve solo su evento")
    void searchPublic_filterByArtistId_returnsMatchingEvent() {
        factory.approvedBarcelonaBoikot();
        factory.approvedMadridAgainstYou();

        var page = service.searchPublic(Optional.of(""), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                                        Optional.of(factory.againstYou().getId()), PageRequest.of(0, 10));

        assertThat(page.getContent()).singleElement().extracting(EventPublicListItemDto::cityName).isEqualTo(TestConstants.MADRID);
    }

    @Test
    @DisplayName("searchPublic: si no hay resultados y query tiene >=2 tokens, intenta fallback")
    void searchPublic_withMultipleTokens_usesFallbackWhenEmpty() {
        factory.approvedBarcelonaBoikot();
        factory.approvedMadridAgainstYou();

        var page = service.searchPublic(Optional.of("palau madri"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                                        Optional.empty(), PageRequest.of(0, 10));

        /*
         * Como no hay eventos que coincidan con "palau" AND "madri",
         * entra en el fallback para buscar con OR y devuelve ambos conciertos.
         */
        assertThat(page.getContent()).hasSize(2)
                                     .extracting(EventPublicListItemDto::cityName)
                                     .containsExactlyInAnyOrder(TestConstants.MADRID, TestConstants.BARCELONA);
    }

    /*
     *  getPublicById
     */

    @Test
    @DisplayName("getPublicById: solo eventos APPROVED son visibles (PENDING -> NotFoundException)")
    void getPublicById_onlyApprovedEventsVisible() {
        var approved = factory.approvedBarcelonaBoikot();
        var pending = factory.pendingValenciaLosDeMarras();

        var dto = service.getPublicById(approved.getId());

        assertThat(dto.id()).isEqualTo(approved.getId());
        var pendingId = pending.getId();
        assertThatThrownBy(() -> service.getPublicById(pendingId)).isInstanceOf(NotFoundException.class)
                                                                  .hasMessage(ErrorConstants.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("getPublicById: si no existe -> 404 NotFoundException")
    void getPublicById_notFound_throws() {
        UUID missing = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        assertThatThrownBy(() -> service.getPublicById(missing)).isInstanceOf(NotFoundException.class)
                                                                .hasMessage(ErrorConstants.EVENT_NOT_FOUND);
    }

    /*
     *  listMine
     */

    @Test
    @DisplayName("listMine: size demasiado grande -> 400 BadRequestException")
    void listMine_pageSizeTooLarge_throws() {
        var pageable = PageRequest.of(0, 10_000);
        assertThatThrownBy(() -> service.listMine(MeEventTabEnum.ALL, pageable)).isInstanceOf(BadRequestException.class)
                                                                                .hasMessage(ErrorConstants.PAGE_SIZE_TOO_LARGE);
    }

    @Test
    @DisplayName("listMine: tab CHANGES -> solo eventos NEEDS_CHANGES del usuario")
    void listMine_tabChanges_returnsOnlyNeedsChangesEvents() {
        factory.needsChangesMadridAgainstYou();
        factory.approvedMadridAgainstYou();
        factory.pendingMadridAgainstYou();

        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));

        var page = service.listMine(MeEventTabEnum.CHANGES, PageRequest.of(0, 10));

        assertThat(page.getContent()).singleElement().extracting(EventPrivateListItemDto::status).isEqualTo(EventStatus.NEEDS_CHANGES);
    }

    @Test
    @DisplayName("listMine: tab PENDING -> solo eventos PENDING_MODERATION del usuario")
    void listMine_tabPending_returnsOnlyPendingEvents() {
        factory.pendingMadridAgainstYou();
        factory.approvedMadridAgainstYou();
        factory.needsChangesMadridAgainstYou();

        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));

        var page = service.listMine(MeEventTabEnum.PENDING, PageRequest.of(0, 10));

        assertThat(page.getContent()).singleElement().extracting(EventPrivateListItemDto::status).isEqualTo(EventStatus.PENDING_MODERATION);
    }

    @Test
    @DisplayName("listMine: tab OTHERS -> excluye NEEDS_CHANGES y PENDING_MODERATION")
    void listMine_tabOthers_excludesChangesAndPending() {
        factory.approvedMadridAgainstYou();
        factory.approvedBarcelonaBoikot();
        factory.pendingMadridAgainstYou();
        factory.needsChangesMadridAgainstYou();

        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));

        var page = service.listMine(MeEventTabEnum.OTHERS, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2).extracting(EventPrivateListItemDto::status).containsOnly(EventStatus.APPROVED);
    }

    @Test
    @DisplayName("listMine: futuros primero, pasados al final")
    void listMine_ordersWithFutureFirst() {
        factory.approvedBarcelonaBoikot();
        factory.approvedMadridAgainstYou();
        factory.approvedValenciaPast();

        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_USER_ID));

        var page = service.listMine(MeEventTabEnum.ALL, PageRequest.of(0, 10));

        // Los futuros deben aparecer antes que cualquier pasado y ordenados
        assertThat(page.getContent()).hasSize(3)
                                     .extracting(EventPrivateListItemDto::cityName)
                                     .containsExactly(TestConstants.MADRID, TestConstants.BARCELONA, "València");
    }
}
