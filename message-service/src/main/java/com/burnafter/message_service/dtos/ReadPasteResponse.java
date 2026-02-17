package com.burnafter.message_service.dtos;

public record ReadPasteResponse(
        boolean ok,
        String iv,
        String ciphertext,
        String pad
) {}
