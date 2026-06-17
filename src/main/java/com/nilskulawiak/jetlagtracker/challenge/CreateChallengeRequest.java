package com.nilskulawiak.jetlagtracker.challenge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateChallengeRequest(
        @NotBlank String name,
        @NotNull @Min(0) Integer xCoordinate,
        @NotNull @Min(0) Integer yCoordinate,
        @NotNull @Positive Integer reward,
        @NotNull ChallengeStatus status,
        @NotBlank String description,
        @NotNull ChallengeType challengeType) {

}
