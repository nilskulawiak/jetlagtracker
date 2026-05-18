package com.nilskulawiak.jetlagtracker.station;

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

}
