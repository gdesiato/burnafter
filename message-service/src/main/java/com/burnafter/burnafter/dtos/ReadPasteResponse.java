package com.burnafter.burnafter.dtos;

public record ReadPasteResponse(
        boolean ok,
        String iv,
        String ciphertext,
        String pad
) {}
