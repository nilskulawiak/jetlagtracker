package com.nilskulawiak.jetlagtracker.game;

import jakarta.validation.constraints.NotBlank;

public record CreateGameRequest(
        @NotBlank String name
) {
}
