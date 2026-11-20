package com.burnafter.message_service.resilience;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChaosInjectorTest {

    @Test
    void doesNothingWhenDisabled() {
        var injector = new ChaosInjector(new ChaosProperties(false, 1.0, 0, 0), new SimpleMeterRegistry());
        assertDoesNotThrow(injector::beforeDelivery);
    }

    @Test
    void failsEveryDeliveryAtFailureRateOne() {
        var registry = new SimpleMeterRegistry();
        var injector = new ChaosInjector(new ChaosProperties(true, 1.0, 0, 0), registry);

        assertThrows(RuntimeException.class, injector::beforeDelivery);
        assertEquals(1, registry.counter("chaos.injected.failures").count());
    }

    @Test
    void delaysEveryDeliveryAtDelayRateOne() {
        var registry = new SimpleMeterRegistry();
        var injector = new ChaosInjector(new ChaosProperties(true, 0, 1.0, 50), registry);

        long start = System.nanoTime();
        injector.beforeDelivery();

        assertEquals(1, registry.counter("chaos.injected.delays").count());
        assertEquals(true, (System.nanoTime() - start) / 1_000_000 >= 50);
    }
}
