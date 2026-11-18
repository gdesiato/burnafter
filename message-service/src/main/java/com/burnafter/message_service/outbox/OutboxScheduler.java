package com.burnafter.message_service.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OutboxScheduler {

    private final OutboxProcessor processor;
    private final int batchSize;

    public OutboxScheduler(OutboxProcessor processor,
                           @Value("${outbox.batch-size:200}") int batchSize) {
        this.processor = processor;
        this.batchSize = batchSize;
    }

    // Previously hardcoded to 50 events every 5000 ms (keys didn't match application.yml),
    // capping delivery at ~10 events/s per instance.
    @Scheduled(fixedDelayString = "${outbox.processing-delay-ms:500}")
    public void trigger() {
        processor.processBatch(batchSize);
    }
}
