package com.burnafter.message_service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePasteRequest {

    // Zero-knowledge payload
    // Base64(GCM ciphertext)
    @NotBlank
    @Size(max = 400_000)
    public String ciphertext;

    // Base64(12-byte IV)
    @NotBlank
    @Size(max = 256)
    public String iv;

    @NotBlank
    @Size(max = 20) // "10min","1h","24h","7d"
    public String expiresIn = "24h";

    @Min(1)
    @Max(10)
    public int views = 1;

    public boolean burnAfterRead = false;

    @NotBlank
    @Size(max = 20)
    public String kind = "TEXT";
}
