package com.burnafter.message_service.resilience;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fault injection for the experiments: makes calls to the audit service randomly
 * fail or slow down, as an unreliable network would. It runs inside the circuit
 * breaker, so injected failures count towards opening it, exactly like real ones.
 * Does nothing unless resilience.chaos.enabled=true.
 */
@Component
public class ChaosInjector {

    private static final Logger log = LoggerFactory.getLogger(ChaosInjector.class);

    private final ChaosProperties properties;
    private final Counter injectedFailures;
    private final Counter injectedDelays;

    public ChaosInjector(ChaosProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.injectedFailures = meterRegistry.counter("chaos.injected.failures");
        this.injectedDelays = meterRegistry.counter("chaos.injected.delays");
    }

    /** Throws (failureRate of the time) or sleeps (delayRate of the time), otherwise returns at once. */
    public void beforeDelivery() {
        if (!properties.enabled()) {
            return;
        }

        double r = ThreadLocalRandom.current().nextDouble();

        if (r < properties.failureRate()) {
            log.warn("CHAOS: injected failure");
            injectedFailures.increment();
            throw new RuntimeException("CHAOS injected failure");
        }

        if (r < properties.failureRate() + properties.delayRate()) {
            log.warn("CHAOS: injected delay of {} ms", properties.delayMs());
            injectedDelays.increment();
            try {
                Thread.sleep(properties.delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
