package com.burnafter.message_service.exception;

public enum InvalidPasteReason {
    INVALID_BASE64,
    INVALID_IV_LENGTH,
    CIPHERTEXT_TOO_LARGE,
    ONLY_TEXT_SUPPORTED
}
