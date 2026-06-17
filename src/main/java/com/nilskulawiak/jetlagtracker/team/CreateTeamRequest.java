package com.nilskulawiak.jetlagtracker.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank String name, @NotBlank String color, @Min(0) Integer startingChips) {

}
