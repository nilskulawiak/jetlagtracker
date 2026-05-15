package com.nilskulawiak.jetlagtracker.challenge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChallengeRequest(
        @NotBlank String name,
        @NotNull Double xCoordinate,
        @NotNull Double yCoordinate,
        @NotNull Integer rewardChips,
        @NotBlank String status) {

}
