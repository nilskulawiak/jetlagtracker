package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

public record StationResponse(
        UUID id,
        UUID gameId,
        String name,
        Double xCoordinate,
        Double yCoordinate) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(),
                station.getGame().getId(),
                station.getName(),
                station.getXCoordinate(),
                station.getYCoordinate());
    }
}
