package com.burnafter.message_service.outbox;

import com.burnafter.message_service.service.AuditDeliveryService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.List;

@Service
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxClaimService claimService;
    private final AuditDeliveryService deliveryService;
    private final OutboxStateService outboxStateService;

    private final Counter processedCounter;
    private final Counter retryCounter;
    private final Counter deadCounter;
    private final Counter cbOpenCounter;

    private final Timer deliveryTimer;
    private final DistributionSummary batchSizeSummary;

    private final CircuitBreaker circuitBreaker;

    @Value("${app.instance-id}")
    private String instanceId;

    public OutboxProcessor(OutboxClaimService claimService,
                           AuditDeliveryService deliveryService,
                           OutboxRepository outboxRepository,
                           OutboxStateService outboxStateService,
                           MeterRegistry meterRegistry,
                           CircuitBreakerRegistry circuitBreakerRegistry) {

        this.claimService = claimService;
        this.deliveryService = deliveryService;
        this.outboxStateService = outboxStateService;

        this.processedCounter = meterRegistry.counter("outbox.events.processed");
        this.retryCounter = meterRegistry.counter("outbox.events.retry");
        this.deadCounter = meterRegistry.counter("outbox.events.dead");
        this.cbOpenCounter = meterRegistry.counter("outbox.circuitbreaker.open");

        this.deliveryTimer = meterRegistry.timer("outbox.delivery.duration");

        this.batchSizeSummary = DistributionSummary
                .builder("outbox.batch.size")
                .register(meterRegistry);

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("auditService");

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("CircuitBreaker state transition: {}",
                                event.getStateTransition()));
    }

    public void processBatch(int batchSize) {
        List<OutboxEvent> events = claimService.claimBatch(batchSize);

        batchSizeSummary.record(events.size());

        for (OutboxEvent event : events) {
            try {
                circuitBreaker.executeRunnable(() ->
                        deliveryTimer.record(() ->
                                deliveryService.deliver(event)
                        )
                );

                outboxStateService.updateSuccess(event.getId());
                processedCounter.increment();

            } catch (CallNotPermittedException ex) {

                log.warn("Circuit breaker OPEN — requeue event {}", event.getId());
                cbOpenCounter.increment();
                outboxStateService.requeue(event.getId());

            } catch (Exception ex) {

                log.warn("Delivery failed for event {} on instance {}", event.getId(), instanceId);

                boolean isDead = outboxStateService.updateFailure(event.getId(), ex);
                retryCounter.increment();

                if (isDead) {
                    deadCounter.increment();
                }
            }
        }
    }
}