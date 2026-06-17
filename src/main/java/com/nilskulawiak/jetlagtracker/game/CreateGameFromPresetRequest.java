package com.nilskulawiak.jetlagtracker.game;

import java.util.List;

import com.nilskulawiak.jetlagtracker.team.CreateTeamRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGameFromPresetRequest(@NotBlank String presetId, @NotBlank String name, @NotNull @Valid List<CreateTeamRequest> teams) {

}
