package com.burnafter.message_service.resilience;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.function.Supplier;

@Component("chaosStrategy")
public class ChaosResilienceStrategy implements ResilienceStrategy {

    private final Random random = new Random();
    private final Counter injectedFailureCounter;
    private final Counter injectedDelayCounter;
    private final ChaosProperties properties;

    public ChaosResilienceStrategy(MeterRegistry meterRegistry, ChaosProperties properties) {

        this.injectedFailureCounter = meterRegistry.counter("chaos.injected.failures");
        this.injectedDelayCounter = meterRegistry.counter("chaos.injected.delays");
        this.properties = properties;
    }

    @Override
    public <T> T execute(Supplier<T> supplier) {

        double r = random.nextDouble();

        // 15% exception injection
        if (r < properties.getFailureRate()) {
            System.out.println("CHAOS: injected failure");
            injectedFailureCounter.increment();
            throw new RuntimeException("CHAOS injected failure");
        }

        // 10% artificial delay
        if (r < properties.getFailureRate() + properties.getDelayRate()) {
            System.out.println("CHAOS: injected delay");
            injectedDelayCounter.increment();
            try {
                Thread.sleep(properties.getDelayMs());;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return supplier.get();
    }
}