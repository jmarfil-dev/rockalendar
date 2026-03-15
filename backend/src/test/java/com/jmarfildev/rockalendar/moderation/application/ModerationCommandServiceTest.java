package com.jmarfildev.rockalendar.moderation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.events.domain.Event;
import com.jmarfildev.rockalendar.events.domain.EventStatus;
import com.jmarfildev.rockalendar.events.persistence.EventRepository;
import com.jmarfildev.rockalendar.moderation.domain.ModerationAction;
import com.jmarfildev.rockalendar.moderation.persistence.ModerationActionRepository;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;
import com.jmarfildev.rockalendar.support.TestDataFactory;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
class ModerationCommandServiceTest extends AbstractPostgresTest {

    @Autowired
    ModerationCommandService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    TestDataFactory factory;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    PlatformTransactionManager txManager;

    @MockitoSpyBean
    ModerationActionRepository moderationActionRepository;
    @MockitoBean
    CurrentUser currentUser;

    private final String MOCK_REASON = "Mock Reason to archive event";

    @BeforeEach
    void cleanDb() {
        cleaner.truncateMutableTables();
        when(currentUser.userId())
                .thenReturn(UUID.fromString(TestConstants.MOCK_MODERATOR_ID));
    }

    @Test
    @DisplayName("approve: si falla guardar ModerationAction -> rollback y event sigue PENDING_MODERATION")
    void approve_actionSaveFails_rollsBackEvent() {
        var event = factory.pendingMadridAgainstYou();
        UUID eventId = event.getId();

        doThrow(new DataIntegrityViolationException("concierto de reguetón"))
                .when(moderationActionRepository)
                .saveAndFlush(any(ModerationAction.class));

        assertThatThrownBy(() -> service.approve(eventId, null))
                .isInstanceOf(DataIntegrityViolationException.class);

        Event persisted = eventRepository.findById(eventId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(EventStatus.PENDING_MODERATION);
        assertThat(persisted.getId()).isEqualTo(eventId);
    }

    /**
     * Para probar esta excepción hay que usar concurrencia de 2 hilos.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("approve: intentar aprobar dos veces el mismo evento -> ConflictException")
    void approve_sameEventTwice_concurrent_throws() throws Exception {
        UUID eventId = factory.pendingMadridAgainstYou().getId();

        var barrier = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Callable<Throwable> task = () -> {
                var tx = new TransactionTemplate(txManager);
                tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                try {
                    tx.executeWithoutResult(status -> {
                        // Espera a que ambos hilos estén listos, dentro de su propia tx
                        try {
                            barrier.await(5, TimeUnit.SECONDS);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        service.approve(eventId, null);
                        // El commit ocurre al salir del executeWithoutResult
                    });
                    return null; // Éxito
                }
                catch (Throwable t) {
                    // Devuelve la excepción para analizar fuera
                    return t;
                }
            };

            Future<Throwable> f1 = pool.submit(task);
            Future<Throwable> f2 = pool.submit(task);

            var t1 = f1.get(15, TimeUnit.SECONDS);
            var t2 = f2.get(15, TimeUnit.SECONDS);

            // Verifica que hay una transacción null (OK) y otra con ConflictException
            var errors = Stream.of(t1, t2).filter(Objects::nonNull).toList();
            var oks = Stream.of(t1, t2).filter(Objects::isNull).toList();
            assertThat(oks).hasSize(1);
            assertThat(errors).hasSize(1);

            Throwable err = errors.get(0);
            assertThat(err).isInstanceOf(ConflictException.class);
            assertThat(err.getMessage()).isEqualTo(ErrorConstants.EVENT_ALREADY_MOD);
        }
        finally {
            pool.shutdownNow();
        }
    }
}
