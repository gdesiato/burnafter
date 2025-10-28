package com.burnafter.burnafter.dtos;

public record ReadPasteResponse(String iv, String ciphertext, Integer viewsRemaining) {}

