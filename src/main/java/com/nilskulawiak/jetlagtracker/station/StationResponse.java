package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

public record StationResponse(
        UUID id,
        UUID gameId,
        String name,
        Integer xCoordinate,
        Integer yCoordinate) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(),
                station.getGame().getId(),
                station.getName(),
                station.getXCoordinate(),
                station.getYCoordinate());
    }
}
