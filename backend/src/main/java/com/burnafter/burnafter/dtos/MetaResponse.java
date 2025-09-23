package com.burnafter.burnafter.dtos;

import java.time.Instant;

public record MetaResponse(String kind, Instant expiresAt, int remaining, boolean protectedByPassword) {}
