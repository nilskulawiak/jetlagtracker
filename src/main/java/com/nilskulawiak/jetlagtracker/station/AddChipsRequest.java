package com.nilskulawiak.jetlagtracker.station;

import java.util.UUID;

import jakarta.validation.constraints.Positive;

public record AddChipsRequest(@Positive Integer chips, UUID teamId) {
}
