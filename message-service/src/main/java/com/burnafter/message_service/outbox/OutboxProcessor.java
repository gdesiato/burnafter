package com.burnafter.message_service.outbox;

import com.burnafter.message_service.service.AuditDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxClaimService claimService;
    private final AuditDeliveryService deliveryService;

    @Value("${app.instance-id}")
    private String instanceId;

    public OutboxProcessor(OutboxClaimService claimService,
                           AuditDeliveryService deliveryService) {
        this.claimService = claimService;
        this.deliveryService = deliveryService;
    }

    public void processBatch(int batchSize) {

        List<OutboxEvent> events =
                claimService.claimBatch(batchSize);

        for (OutboxEvent event : events) {

            log.info("Instance {} processing event {}",
                    instanceId,
                    event.getId());

            deliveryService.deliver(event);
        }
    }
}
