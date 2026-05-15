package com.nilskulawiak.jetlagtracker.team;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank String name, @NotBlank String color, Integer startingChips) {
    
}
