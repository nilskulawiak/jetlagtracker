package com.nilskulawiak.jetlagtracker.challenge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChallengeRequest(
        @NotBlank String name,
        @NotNull Integer xCoordinate,
        @NotNull Integer yCoordinate,
        @NotNull Integer reward,
        ChallengeStatus status,
        @NotBlank String description,
        @NotNull ChallengeType challengeType) {

}
