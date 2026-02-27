package com.jmarfildev.rockalendar.moderation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.jmarfildev.rockalendar.common.Constants;
import com.jmarfildev.rockalendar.common.error.BadRequestException;
import com.jmarfildev.rockalendar.common.error.ErrorMessages;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 *
 */
@DataJpaTest
@Import({ ModerationQueryService.class, DatabaseCleaner.class, TestDataFactory.class })
public class ModerationQueryServiceTest extends AbstractPostgresTest {

    @Autowired
    ModerationQueryService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
    }

    @Test
    @DisplayName("listPending: page demadsado grande -> BadRequest")
    void listPending_pageSizeTooLarge_throws() {
        assertThatThrownBy(() -> service.listPending(PageRequest.of(0, Constants.maxPageSize + 1)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.PAGE_SIZE_TOO_LARGE);
    }

    // Este test parece redundante con los de contrato pero aquí además comprueba que el evento se mappea
    @Test
    @DisplayName("listPending: ok -> eventos PENDING_MODERATION ordenados por submittedAt ASC")
    void listPending_ok_returnPendingOrderBySubmittedAtAsc() {
        var eventV = factory.pendingValenciaLosDeMarras();
        var eventM = factory.pendingMadridAgainstYou();
        factory.approvedBarcelonaBoikot();
        factory.rejectedValenciaMafalda();

        var page = service.listPending(PageRequest.of(0, 20));

        assertThat(page.getContent())
                .hasSize(2)
                .satisfiesExactly(
                        e -> {
                            assertThat(e.id()).isEqualTo(eventV.getId());
                            assertThat(e.submittedAt()).isNotNull();
                        },
                        e -> {
                            assertThat(e.id()).isEqualTo(eventM.getId());
                            assertThat(e.submittedAt()).isNotNull();
                        });
    }

    @Test
    @DisplayName("listArchived: page demasiado grande -> 400 BadRequest")
    void listArchived_pageSizeTooLarge_throws() {
        assertThatThrownBy(() -> service.listArchived(PageRequest.of(0, Constants.maxPageSize + 1)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorMessages.PAGE_SIZE_TOO_LARGE);
    }

    // Este test parece redundante con los de contrato pero aquí además comprueba que el evento se mappea
    @Test
    @DisplayName("listArchived: ok -> eventos REJECTED, HIDEN y CANCELED ordenados por moderatedAt DES")
    void listArchived_ok_retuenArchivedOrderByModeratedAtDesc() {
        factory.pendingValenciaLosDeMarras();
        factory.rejectedValenciaMafalda();
        factory.hiddenMadridSoziedadAlkoholika();
        factory.canceledBarcelonaManifa();

        var page = service.listArchived(PageRequest.of(0, 20, Sort.by(Sort.Order.desc("moderated"))));

        assertThat(page.getContent())
                .hasSize(3)
                .satisfiesExactly(
                        e -> assertThat(e.status()).isEqualTo(EventStatus.CANCELED),
                        e -> assertThat(e.status()).isEqualTo(EventStatus.HIDDEN),
                        e -> assertThat(e.status()).isEqualTo(EventStatus.REJECTED));
    }
}
