package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.CreatePasteRequest;
import com.burnafter.message_service.dtos.CreatePasteResponse;
import com.burnafter.message_service.metrics.MetricsService;
import com.burnafter.message_service.model.Paste;
import com.burnafter.message_service.outbox.OutboxEvent;
import com.burnafter.message_service.outbox.OutboxRepository;
import com.burnafter.message_service.repository.PasteRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;
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

        service = new PasteService(
                pasteRepository,
                outboxRepository,
                new MetricsService(new SimpleMeterRegistry()));

        service.maxTextBytes = 20000;
        service.defaultTtlMinutes = 60;
        service.maxTtlMinutes = 120;
        ReflectionTestUtils.setField(service, "publicBaseUrl", "https://burn.test");
    }

    @Test
    void create_validRequest_createsPasteAndOutboxEvent() {
        CreatePasteResponse res = service.create(validRequest(), "http://localhost");

        assertNotNull(res.id());
        assertTrue(res.readUrl().startsWith("https://burn.test/p/"));
        assertEquals(3, res.viewsLeft());
        assertTrue(res.expireAt().isAfter(Instant.now()));

        verify(pasteRepository).save(any(Paste.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    private CreatePasteRequest validRequest() {
        CreatePasteRequest r = new CreatePasteRequest();
        r.kind = "TEXT";
        r.views = 3;
        r.expiresIn = "10min";
        r.burnAfterRead = false;
        r.iv = Base64.getEncoder().encodeToString(new byte[12]);
        r.ciphertext = Base64.getEncoder().encodeToString("secret".getBytes());
        return r;
    }
}
