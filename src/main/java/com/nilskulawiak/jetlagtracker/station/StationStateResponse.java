package com.nilskulawiak.jetlagtracker.station;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record StationStateResponse(
        UUID id,
        String name,
        int xCoordinate,
        int yCoordinate,
        UUID ownerTeamId,
        List<StationChipStateResponse> chips
) {

    public static StationStateResponse from(Station station, List<StationChipState> chipStates) {
        UUID ownerTeamId = chipStates.stream()
                .max(Comparator.comparingInt(StationChipState::getChips))
                .map(cs -> cs.getTeam().getId())
                .orElse(null);

        return new StationStateResponse(
                station.getId(),
                station.getName(),
                station.getXCoordinate(),
                station.getYCoordinate(),
                ownerTeamId,
                chipStates.stream().map(StationChipStateResponse::from).toList());
    }
}
