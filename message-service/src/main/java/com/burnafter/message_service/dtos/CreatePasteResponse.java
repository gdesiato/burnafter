package com.burnafter.message_service.dtos;

import java.time.Instant;

public record CreatePasteResponse(String id, Instant expireAt, int viewsLeft) {}
