package com.burnafter.burnafter.error;

import com.burnafter.burnafter.TestSecurityConfig;
import com.burnafter.burnafter.controller.PasteController;
import com.burnafter.burnafter.dtos.CreatePasteRequest;
import com.burnafter.burnafter.dtos.CreatePasteResponse;
import com.burnafter.burnafter.exception.InvalidPasteException;
import com.burnafter.burnafter.exception.InvalidPasteReason;
import com.burnafter.burnafter.service.PasteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasteController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasteService pasteService;

    @Test
    void create_happyPath_returns201WithHeadersAndBody() throws Exception {
        // given
        CreatePasteResponse response = new CreatePasteResponse(
                "123e4567-e89b-12d3-a456-426614174000",
                "https://example.com/p/123e4567-e89b-12d3-a456-426614174000",
                Instant.now().plusSeconds(600),
                1
        );

        when(pasteService.create(any(), any()))
                .thenReturn(response);

        // when + then
        mockMvc.perform(post("/api/pastes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "kind": "TEXT",
                  "ciphertext": "YQ==",
                  "iv": "AAAAAAAAAAAA",
                  "views": 1
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", response.readUrl()))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.readUrl").value(response.readUrl()))
                .andExpect(jsonPath("$.viewsLeft").value(1));
    }

    @Test
    void invalidPasteException_returns400() throws Exception {
        when(pasteService.create(any(), any()))
                .thenThrow(new InvalidPasteException(InvalidPasteReason.INVALID_BASE64));

        mockMvc.perform(post("/api/pastes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "kind": "TEXT",
                  "ciphertext": "YQ=!",
                  "iv": "AAAAAAAAAAAA",
                  "views": 1
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INVALID_BASE64"));
    }

    @Test
    void unexpectedException_returns500() throws Exception {
        when(pasteService.create(any(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/pastes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "kind": "TEXT",
                  "ciphertext": "YQ=!",
                  "iv": "AAAAAAAAAAAA",
                  "views": 1
                }
                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}
