package com.nilskulawiak.jetlagtracker.station;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatchStationRequest(@Size(min = 1) String name, @Min(0) Integer xCoordinate, @Min(0) Integer yCoordinate) {
}
