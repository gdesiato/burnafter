package com.burnafter.message_service.dtos;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        String path,
        Instant timestamp
) {}
