package com.burnafter.burnafter.dtos;

import java.time.Instant;

public record MetaResponse(
        String kind,
        Instant expireAt,
        int viewsLeft
) {}
