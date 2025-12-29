package com.burnafter.burnafter.error;

import com.burnafter.burnafter.TestSecurityConfig;
import com.burnafter.burnafter.controller.PasteController;
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
    void invalidPasteException_returns400() throws Exception {
        when(pasteService.create(any(), any()))
                .thenThrow(new InvalidPasteException(InvalidPasteReason.INVALID_BASE64));

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
                  "ciphertext": "YQ==",
                  "iv": "AAAAAAAAAAAA",
                  "views": 1
                }
                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}
