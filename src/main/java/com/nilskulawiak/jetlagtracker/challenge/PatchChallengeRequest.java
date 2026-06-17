package com.nilskulawiak.jetlagtracker.challenge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PatchChallengeRequest(
        @Size(min = 1) String name,
        @Size(min = 1) String description,
        @Positive Integer reward,
        @Min(0) Integer xCoordinate,
        @Min(0) Integer yCoordinate,
        ChallengeType challengeType,
        ChallengeStatus status) {
}
