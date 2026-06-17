package com.nilskulawiak.jetlagtracker.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PatchTeamRequest(@Size(min = 1) String name, @Size(min = 1) String color, @Min(0) Integer availableChips) {
}
