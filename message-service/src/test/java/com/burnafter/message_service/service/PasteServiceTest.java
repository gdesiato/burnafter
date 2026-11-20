package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.CreatePasteRequest;
import com.burnafter.message_service.dtos.CreatePasteResponse;
import com.burnafter.message_service.exception.InvalidPasteException;
import com.burnafter.message_service.exception.InvalidPasteReason;
import com.burnafter.message_service.metrics.MetricsService;
import com.burnafter.message_service.model.Paste;
import com.burnafter.message_service.outbox.OutboxEvent;
import com.burnafter.message_service.outbox.OutboxRepository;
import com.burnafter.message_service.repository.PasteRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasteServiceTest {

    private PasteRepository pasteRepository;
    private OutboxRepository outboxRepository;
    private PasteService service;

    @BeforeEach
    void setUp() {
        pasteRepository = mock(PasteRepository.class);
        outboxRepository = mock(OutboxRepository.class);

        // JPA assigns the id on save; simulate that on the passed-in entity.
        when(pasteRepository.save(any(Paste.class))).thenAnswer(inv -> {
            Paste p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
            return p;
        });

        service = new PasteService(pasteRepository, outboxRepository,
                new MetricsService(new SimpleMeterRegistry()));
    }

    // --- create ---

    @Test
    void create_savesPasteAndOutboxEvent() {
        CreatePasteResponse res = service.create(validRequest());

        assertNotNull(res.id());
        assertEquals(3, res.viewsLeft());
        assertTrue(res.expireAt().isAfter(Instant.now().plus(Duration.ofMinutes(9))));

        verify(pasteRepository).save(any(Paste.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void create_rejectsInvalidBase64() {
        CreatePasteRequest req = validRequest();
        req.ciphertext = "not base64!";

        assertReason(InvalidPasteReason.INVALID_BASE64, req);
    }

    @Test
    void create_rejectsWrongIvLength() {
        CreatePasteRequest req = validRequest();
        req.iv = Base64.getEncoder().encodeToString(new byte[16]);

        assertReason(InvalidPasteReason.INVALID_IV_LENGTH, req);
    }

    @Test
    void create_rejectsOversizedCiphertext() {
        CreatePasteRequest req = validRequest();
        req.ciphertext = Base64.getEncoder().encodeToString(new byte[PasteService.MAX_CIPHERTEXT_BYTES + 1]);

        assertReason(InvalidPasteReason.CIPHERTEXT_TOO_LARGE, req);
    }

    @Test
    void create_rejectsUnknownExpiry() {
        CreatePasteRequest req = validRequest();
        req.expiresIn = "100y";

        assertReason(InvalidPasteReason.INVALID_EXPIRY, req);
    }

    // --- data ---

    @Test
    void data_returnsCiphertextAndKeepsPasteWhileViewsRemain() {
        Paste paste = paste(Instant.now().plusSeconds(60), 2);
        when(pasteRepository.findById(any())).thenReturn(Optional.of(paste));

        assertTrue(service.data(UUID.randomUUID()).isPresent());
        assertEquals(1, paste.getViewsLeft());
        verify(pasteRepository, never()).delete(any());
    }

    @Test
    void data_deletesPasteOnLastView() {
        Paste paste = paste(Instant.now().plusSeconds(60), 1);
        when(pasteRepository.findById(any())).thenReturn(Optional.of(paste));

        assertTrue(service.data(UUID.randomUUID()).isPresent());
        verify(pasteRepository).delete(paste);
    }

    @Test
    void data_expiredPasteIsDeletedAndNotReturned() {
        // Returns empty instead of throwing: an exception would roll back the delete.
        Paste paste = paste(Instant.now().minusSeconds(1), 1);
        when(pasteRepository.findById(any())).thenReturn(Optional.of(paste));

        assertTrue(service.data(UUID.randomUUID()).isEmpty());
        verify(pasteRepository).delete(paste);
    }

    @Test
    void data_unknownIdIsEmpty() {
        when(pasteRepository.findById(any())).thenReturn(Optional.empty());

        assertTrue(service.data(UUID.randomUUID()).isEmpty());
    }

    // --- helpers ---

    private void assertReason(InvalidPasteReason expected, CreatePasteRequest req) {
        InvalidPasteException ex = assertThrows(InvalidPasteException.class, () -> service.create(req));
        assertEquals(expected, ex.getReason());
        verifyNoInteractions(outboxRepository);
    }

    private static Paste paste(Instant expireAt, int views) {
        return new Paste("Y2lwaGVy", "aXY=", Instant.now(), expireAt, views);
    }

    private static CreatePasteRequest validRequest() {
        CreatePasteRequest r = new CreatePasteRequest();
        r.views = 3;
        r.expiresIn = "10min";
        r.iv = Base64.getEncoder().encodeToString(new byte[12]);
        r.ciphertext = Base64.getEncoder().encodeToString("secret".getBytes());
        return r;
    }
}
