package com.burnafter.message_service.outbox;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OutboxScheduler {

    private final OutboxProcessor processor;

    public OutboxScheduler(OutboxProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${outbox.delay-ms:5000}")
    public void trigger() {
        processor.processBatch(50);
    }
}
