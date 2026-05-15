package com.nilskulawiak.jetlagtracker.station;

import jakarta.validation.constraints.NotBlank;

public record CreateStationRequest(@NotBlank String name, @NotBlank Double xCoordinate, @NotBlank Double yCoordinate) {

}
