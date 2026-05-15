package com.nilskulawiak.jetlagtracker.team;

import java.util.UUID;

public record TeamResponse(UUID id, UUID gameId, String name, String color, Integer availableChips) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getGame().getId(),
                team.getName(),
                team.getColor(),
                team.getAvailableChips()
        );
    }
}
