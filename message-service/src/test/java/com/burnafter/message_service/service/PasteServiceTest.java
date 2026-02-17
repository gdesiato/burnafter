package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.CreatePasteRequest;
import com.burnafter.message_service.dtos.CreatePasteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PasteService.class)
@TestPropertySource(properties = {
        "app.maxTextBytes=20000",
        "app.defaultTtlMinutes=60",
        "app.maxTtlMinutes=120",
        "app.publicBaseUrl=https://burn.test"
})
class PasteServiceTest {

    @Autowired
    PasteService service;

    @Test
    void create_validRequest_createsPaste() {
        CreatePasteRequest req = validRequest();

        CreatePasteResponse res = service.create(req, "http://localhost");

        assertNotNull(res);
        assertNotNull(res.id());
        assertTrue(res.readUrl().startsWith("https://burn.test/p/"));
        assertEquals(3, res.viewsLeft());

        assertTrue(res.expireAt().isAfter(Instant.now()));
    }

    private CreatePasteRequest validRequest() {
        CreatePasteRequest r = new CreatePasteRequest();
        r.kind = "TEXT";
        r.views = 3;
        r.expiresIn = "10min";
        r.burnAfterRead = false;

        r.iv = Base64.getEncoder()
                .encodeToString(new byte[12]);

        r.ciphertext = Base64.getEncoder()
                .encodeToString("secret".getBytes());

        return r;
    }
}
