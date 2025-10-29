package com.burnafter.burnafter.dtos;

public record ReadPasteResponse(
        boolean available,          // false for not found / expired / already read
        String iv,                  // null when unavailable
        String ciphertext,          // null when unavailable
        Integer viewsRemaining,     // may be null; keep the field
        String pad                  // random padding to normalize size
) {}
