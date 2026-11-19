package com.burnafter.message_service.outbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackoffTest {

    @Test
    void growsExponentiallyThenCaps() {
        long[] expected = {2, 4, 8, 16, 32, 60, 60};
        for (int attempt = 1; attempt <= expected.length; attempt++) {
            assertEquals(expected[attempt - 1],
                    OutboxStateService.backoffSeconds(2, 60, attempt),
                    "attempt " + attempt);
        }
    }

    @Test
    void neverOverflowsForLargeAttempts() {
        assertEquals(60, OutboxStateService.backoffSeconds(2, 60, 1_000));
    }
}
