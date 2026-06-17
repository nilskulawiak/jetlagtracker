package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddChipsRequest(@Positive Integer chips, @NotNull UUID teamId) {
}
