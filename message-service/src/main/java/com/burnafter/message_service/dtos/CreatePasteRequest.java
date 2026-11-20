package com.burnafter.message_service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePasteRequest {

    /*
    * Base64 of the AES-GCM ciphertext. Coarse cap here (64 KiB of bytes is
    * ~87k Base64 chars); PasteService checks the exact decoded size.
    */
    @NotBlank
    @Size(max = 100_000)
    public String ciphertext;

    // Base64 of the 12-byte IV
    @NotBlank
    @Size(max = 32)
    public String iv;

    // "10min", "1h", "24h" or "7d"
    @NotBlank
    public String expiresIn = "24h";

    @Min(1)
    @Max(10)
    public int views = 1;
}
