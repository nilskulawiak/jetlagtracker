package com.nilskulawiak.jetlagtracker.common.exception;

import java.time.Instant;

public record ErrorResponse(
        String message,
        Instant timestamp
) {
}