package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

public record StationChipStateResponse(
        UUID stationId,
        String stationName,
        UUID teamId,
        String teamName,
        int chipsOnStation,
        int teamAvailableChips
) {
    public static StationChipStateResponse from(StationChipState state) {
        return new StationChipStateResponse(
                state.getStation().getId(),
                state.getStation().getName(),
                state.getTeam().getId(),
                state.getTeam().getName(),
                state.getChips(),
                state.getTeam().getAvailableChips()
        );
    }
}