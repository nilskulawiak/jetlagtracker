package com.nilskulawiak.jetlagtracker.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateGameRequest(
        @NotBlank String name, @Positive Integer mapWidth, @Positive Integer mapHeight, @NotBlank String mapImage
) {
}
