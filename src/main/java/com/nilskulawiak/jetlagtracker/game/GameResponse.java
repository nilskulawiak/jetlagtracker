package com.nilskulawiak.jetlagtracker.game;

import java.time.Instant;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String name,
        String status,
        Instant createdAt
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getStatus(),
                game.getCreatedAt()
        );
    }
}