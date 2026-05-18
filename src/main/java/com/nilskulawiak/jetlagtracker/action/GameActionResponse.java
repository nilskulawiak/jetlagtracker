package com.nilskulawiak.jetlagtracker.action;

import java.time.Instant;
import java.util.UUID;

public record GameActionResponse(
        UUID id,
        UUID gameId,
        GameActionType type,
        String message,
        Instant createdAt
) {
    public static GameActionResponse from(GameAction gameAction) {
        return new GameActionResponse(
                gameAction.getId(),
                gameAction.getGame().getId(),
                gameAction.getType(),
                gameAction.getMessage(),
                gameAction.getCreatedAt());
    }
}
