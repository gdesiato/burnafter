package com.burnafter.message_service.service;

import com.burnafter.message_service.configuration.CorrelationIdFilter;
import com.burnafter.message_service.dtos.CreatePasteRequest;
import com.burnafter.message_service.dtos.CreatePasteResponse;
import com.burnafter.message_service.dtos.DataResponse;
import com.burnafter.message_service.exception.InvalidPasteException;
import com.burnafter.message_service.exception.InvalidPasteReason;
import com.burnafter.message_service.metrics.MetricsService;
import com.burnafter.message_service.model.Paste;
import com.burnafter.message_service.outbox.OutboxEvent;
import com.burnafter.message_service.outbox.OutboxRepository;
import com.burnafter.message_service.repository.PasteRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasteService {

    // AES-GCM ciphertext = plaintext + 16-byte tag. The UI allows 12,000 characters
    // (at most 4 bytes each in UTF-8, so ~48 KB): 64 KiB leaves headroom.
    static final int MAX_CIPHERTEXT_BYTES = 64 * 1024;
    static final int IV_BYTES = 12; // standard AES-GCM nonce size

    private final PasteRepository pasteRepository;
    private final OutboxRepository outboxRepository;
    private final MetricsService metricsService;

    public PasteService(PasteRepository pasteRepository,
                        OutboxRepository outboxRepository,
                        MetricsService metricsService) {
        this.pasteRepository = pasteRepository;
        this.outboxRepository = outboxRepository;
        this.metricsService = metricsService;
    }

    /**
     * Stores the paste and its outbox event in ONE transaction: either both are
     * saved or neither is, so every created paste is eventually audited.
     */
    @Transactional
    public CreatePasteResponse create(CreatePasteRequest req) {
        validateCrypto(req);

        Instant now = Instant.now();
        Paste paste = new Paste(req.ciphertext, req.iv, now, now.plus(ttl(req.expiresIn)), req.views);
        pasteRepository.save(paste);

        outboxRepository.save(new OutboxEvent(
                paste.getId(),
                "PASTE_CREATED",
                paste.getId().toString(),
                MDC.get(CorrelationIdFilter.MDC_KEY)));

        metricsService.incrementMessagesCreated();

        return new CreatePasteResponse(paste.getId().toString(), paste.getExpireAt(), paste.getViewsLeft());
    }

    /**
     * Returns the encrypted data and uses up one view; the row is deleted when the
     * last view is used. Empty if the paste doesn't exist, has expired, or is gone.
     * Two concurrent reads of the last view: @Version makes one of them fail at commit.
     */
    @Transactional
    public Optional<DataResponse> data(UUID id) {
        Optional<Paste> found = pasteRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Paste paste = found.get();

        if (paste.isExpired()) {
            pasteRepository.delete(paste);
            return Optional.empty();
        }

        paste.consumeView();
        if (paste.isDepleted()) {
            pasteRepository.delete(paste);
        }

        return Optional.of(new DataResponse(paste.getIv(), paste.getCiphertext()));
    }

    @Transactional
    public int purgeExpired() {
        List<Paste> expired = pasteRepository.findByExpireAtBefore(Instant.now());
        pasteRepository.deleteAll(expired);
        return expired.size();
    }

    // The server can't read the data, but it can check it's well-formed and not too big.
    private static void validateCrypto(CreatePasteRequest req) {
        if (decodeB64(req.iv).length != IV_BYTES)
            throw new InvalidPasteException(InvalidPasteReason.INVALID_IV_LENGTH);

        if (decodeB64(req.ciphertext).length > MAX_CIPHERTEXT_BYTES)
            throw new InvalidPasteException(InvalidPasteReason.CIPHERTEXT_TOO_LARGE);
    }

    private static byte[] decodeB64(String s) {
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            throw new InvalidPasteException(InvalidPasteReason.INVALID_BASE64);
        }
    }

    // The four options offered by the UI; anything else is rejected.
    private static Duration ttl(String expiresIn) {
        return switch (expiresIn) {
            case "10min" -> Duration.ofMinutes(10);
            case "1h" -> Duration.ofHours(1);
            case "24h" -> Duration.ofHours(24);
            case "7d" -> Duration.ofDays(7);
            default -> throw new InvalidPasteException(InvalidPasteReason.INVALID_EXPIRY);
        };
    }
}
