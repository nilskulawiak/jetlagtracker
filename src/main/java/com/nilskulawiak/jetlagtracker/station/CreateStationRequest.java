package com.nilskulawiak.jetlagtracker.station;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateStationRequest(@NotBlank String name, @Positive Integer xCoordinate, @Positive Integer yCoordinate) {

}
