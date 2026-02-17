package com.burnafter.message_service;

import com.burnafter.message_service.model.Paste;
import com.burnafter.message_service.repository.PasteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PasteRepositoryIntegrationTest {

    @Autowired
    private PasteRepository repository;

    @Test
    void testContextLoads_andCanPersistPaste() {
        Paste paste = new Paste(
                Paste.Kind.TEXT,
                "ciphertext",
                "ivvalue",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                3,
                false
        );

        Paste saved = repository.save(paste);

        assertThat(saved.getId()).isNotNull();

        Paste found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCiphertext()).isEqualTo("ciphertext");
        assertThat(found.getViewsLeft()).isEqualTo(3);
    }
}
