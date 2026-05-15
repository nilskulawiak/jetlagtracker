package com.nilskulawiak.jetlagtracker.game;

import java.time.Instant;
import java.util.UUID;

public record GameResponse(
        UUID id,
        String name,
        GameStatus status,
        Instant createdAt,
        Integer mapWidth,
        Integer mapHeight,
        String  mapImage
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getStatus(),
                game.getCreatedAt(),
                game.getMapWidth(),
                game.getMapHeight(),
                game.getMapImage()
        );
    }
}