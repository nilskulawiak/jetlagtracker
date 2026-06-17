package com.nilskulawiak.jetlagtracker.game;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PatchGameRequest(@Size(min = 1) String name, @Positive Integer mapWidth, @Positive Integer mapHeight, @Size(min = 1) String mapImage) {
}
