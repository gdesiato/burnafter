package com.burnafter.message_service;

import com.burnafter.message_service.controller.PasteController;
import com.burnafter.message_service.dtos.ReadPasteResponse;
import com.burnafter.message_service.model.Paste;
import com.burnafter.message_service.repository.PasteRepository;
import com.burnafter.message_service.service.PasteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Burn after reading under concurrency: 20 readers hit the same single-view
 * paste at the same moment, and exactly one of them may get the ciphertext.
 *
 * Needs a real Postgres (like the other @SpringBootTest tests): optimistic
 * locking is enforced by the database rejecting a stale-version DELETE.
 */
@SpringBootTest
class ConcurrentReadIntegrationTest {

    private static final int READERS = 20;

    @Autowired private PasteService service;
    @Autowired private PasteController controller;
    @Autowired private PasteRepository repository;

    enum Outcome { GOT_MESSAGE, NOT_FOUND, LOST_RACE }

    /**
     * Service level: shows the mechanism. A reader either gets the message,
     * finds it already gone, or loses the race at commit (@Version conflict).
     */
    @Test
    void onlyOneOfTwentyConcurrentReadersGetsTheMessage() throws Exception {
        UUID id = saveSingleViewPaste();

        List<Outcome> outcomes = runConcurrently(() -> {
            try {
                return service.data(id).isPresent() ? Outcome.GOT_MESSAGE : Outcome.NOT_FOUND;
            } catch (ObjectOptimisticLockingFailureException e) {
                return Outcome.LOST_RACE;
            }
        });

        assertThat(outcomes).hasSize(READERS);
        assertThat(outcomes).filteredOn(o -> o == Outcome.GOT_MESSAGE).hasSize(1);
        assertThat(repository.findById(id)).isEmpty(); // burned
    }

    /**
     * API level: losers of the race must get the same response as "not found",
     * never an error, so a reader learns nothing from losing.
     */
    @Test
    void racingReadersGetTheSameResponseAsAMissingPaste() throws Exception {
        UUID id = saveSingleViewPaste();

        List<ResponseEntity<ReadPasteResponse>> responses =
                runConcurrently(() -> controller.data(id.toString()));

        assertThat(responses).allSatisfy(r -> assertThat(r.getStatusCode().value()).isEqualTo(200));

        List<ReadPasteResponse> winners = responses.stream()
                .map(ResponseEntity::getBody)
                .filter(Objects::nonNull)
                .filter(ReadPasteResponse::ok)
                .toList();

        List<ReadPasteResponse> losers = responses.stream()
                .map(ResponseEntity::getBody)
                .filter(b -> !b.ok())
                .toList();

        assertThat(winners).hasSize(1);
        assertThat(winners.get(0).ciphertext()).isEqualTo("ciphertext");
        assertThat(losers).hasSize(READERS - 1)
                .allSatisfy(b -> {
                    assertThat(b.ciphertext()).isNull();
                    assertThat(b.iv()).isNull();
                });
    }

    private UUID saveSingleViewPaste() {
        Paste paste = new Paste("ciphertext", "ivvalue",
                Instant.now(), Instant.now().plusSeconds(3600), 1);
        return repository.save(paste).getId();
    }

    /** Starts all readers at once (start gate) to maximise contention. */
    private <T> List<T> runConcurrently(Callable<T> reader) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(READERS);
        CountDownLatch startGate = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int i = 0; i < READERS; i++) {
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return reader.call();
                }));
            }
            startGate.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> f : futures) {
                results.add(f.get(30, TimeUnit.SECONDS)); // any unexpected exception fails the test
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
