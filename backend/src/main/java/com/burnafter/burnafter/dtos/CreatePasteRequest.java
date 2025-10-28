package com.burnafter.burnafter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePasteRequest {

    // ---- MODE A: plaintext (legacy) ----
    @Size(max = 20_000)
    public String content;

    // ---- MODE B: encrypted (ZK) ----
    // Base64(GCM ciphertext) + Base64(12-byte IV)
    @Size(max = 400_000)
    public String ciphertext;

    @Size(max = 256)
    public String iv;

    @NotBlank
    @Size(max = 20)               // "10min","1h","24h","7d", etc.
    public String expiresIn = "24h";

    @Min(1) @Max(10)
    public int views = 1;

    public boolean burnAfterRead = false;

    // Only for legacy plaintext password-protected flow (if you keep it)
    @Size(max = 200)
    public String password;

    @NotBlank
    @Size(max = 20)
    public String kind = "TEXT";

    // ---- Validation: exactly one mode must be used ----
    @AssertTrue(message = "Provide either plaintext content OR ciphertext+iv")
    public boolean isValidPayload() {
        boolean hasPlain = content != null && !content.isBlank();
        boolean hasEnc   = (ciphertext != null && !ciphertext.isBlank())
                && (iv != null && !iv.isBlank());
        return hasPlain ^ hasEnc; // xor: one or the other, not both
    }

    public boolean isPlaintext() {
        return content != null && !content.isBlank();
    }

    public boolean isEncrypted() {
        return (ciphertext != null && !ciphertext.isBlank())
                && (iv != null && !iv.isBlank());
    }
}
