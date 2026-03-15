package com.jmarfildev.rockalendar.artist.application;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.jmarfildev.rockalendar.artists.api.dto.CreateArtistRequest;
import com.jmarfildev.rockalendar.artists.application.ArtistCommandService;
import com.jmarfildev.rockalendar.common.error.ConflictException;
import com.jmarfildev.rockalendar.common.error.ErrorConstants;
import com.jmarfildev.rockalendar.common.helper.CurrentUser;
import com.jmarfildev.rockalendar.config.AbstractPostgresTest;
import com.jmarfildev.rockalendar.support.DatabaseCleaner;
import com.jmarfildev.rockalendar.support.TestConstants;

/**
 * @author jmarfil
 *
 */
@SpringBootTest
class ArtistCommandServiceTest extends AbstractPostgresTest {

    @Autowired
    ArtistCommandService service;
    @Autowired
    DatabaseCleaner cleaner;
    @Autowired
    PlatformTransactionManager txManager;

    @MockitoBean
    CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        cleaner.truncateMutableTables();
        when(currentUser.userId()).thenReturn(UUID.fromString(TestConstants.MOCK_MODERATOR_ID));
    }

    /**
     * Para probar esta excepción hay que usar concurrencia de 2 hilos.
     */
    @Test
    @DisplayName("createArtist: dos peticiones concurrentes con el mismo nombre -> una ConflictException, una ok")
    void createArtist_concurrent_sameSlug_oneSucceedsOneThrowsConflict() throws Exception {
        var barrier = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            Callable<Throwable> task = () -> {
                var tx = new TransactionTemplate(txManager);
                tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                try {
                    tx.executeWithoutResult(status -> {
                        try {
                            barrier.await(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        service.createArtist(new CreateArtistRequest("Against You"));
                    });
                    return null; // éxito
                } catch (Throwable t) {
                    return t;
                }
            };

            Future<Throwable> f1 = pool.submit(task);
            Future<Throwable> f2 = pool.submit(task);

            var t1 = f1.get(15, TimeUnit.SECONDS);
            var t2 = f2.get(15, TimeUnit.SECONDS);

            var errors = Stream.of(t1, t2).filter(Objects::nonNull).toList();
            var oks    = Stream.of(t1, t2).filter(Objects::isNull).toList();

            assertThat(oks).hasSize(1);
            assertThat(errors).hasSize(1);

            Throwable err = errors.get(0);
            assertThat(err).isInstanceOf(ConflictException.class);
            assertThat(err.getMessage()).isEqualTo(ErrorConstants.ARTIST_ALREADY_EXISTS);
        } finally {
            pool.shutdownNow();
        }
    }
}
