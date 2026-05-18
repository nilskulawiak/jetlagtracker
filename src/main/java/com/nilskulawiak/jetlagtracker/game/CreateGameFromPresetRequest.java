package com.nilskulawiak.jetlagtracker.game;

import java.util.List;

import com.nilskulawiak.jetlagtracker.team.CreateTeamRequest;

import jakarta.validation.constraints.NotBlank;

public record CreateGameFromPresetRequest(@NotBlank String presetId, @NotBlank String name, List<CreateTeamRequest> teams) {

}
