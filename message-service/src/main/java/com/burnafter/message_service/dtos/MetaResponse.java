package com.burnafter.message_service.dtos;

import java.time.Instant;

public record MetaResponse(
        String kind,
        Instant expireAt,
        int viewsLeft
) {}
