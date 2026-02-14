package com.burnafter.burnafter.exception;

public class InvalidPasteException extends RuntimeException {
    private final InvalidPasteReason reason;

    public InvalidPasteException(InvalidPasteReason reason) {
        this.reason = reason;
    }

    public InvalidPasteReason getReason() {
        return reason;
    }
}

