package com.burnafter.burnafter.dtos;

import java.time.Instant;

public record CreatePasteResponse(String id, String readUrl, Instant expireAt, int viewsLeft) {}
