package com.burnafter.message_service;

import com.burnafter.message_service.service.PasteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PasteCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PasteCleanupJob.class);

    private final PasteService service;

    public PasteCleanupJob(PasteService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60_000) // every minute
    public void purge() {
        int removed = service.purgeExpired();
        if (removed > 0) log.info("Purged {} expired pastes", removed);
    }
}
