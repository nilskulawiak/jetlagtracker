package com.nilskulawiak.jetlagtracker.station;

import jakarta.validation.constraints.Min;

public record SetStationChipsRequest(@Min(0) int chips) {
}
